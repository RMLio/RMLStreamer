package io.rml.framework.flink.util;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.misc.FileFinder;
import org.apache.flink.api.common.cache.DistributedCache;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * MIT License
 * <p>
 * Copyright (C) 2017 - 2022 RDF Mapping Language (RML)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **/
public class FunctionsFlinkUtil {
	final private static Set<String> functionDescriptionLocations = new HashSet<>();
	final private static Set<String> implementationLocations = new HashSet<>();

	final private static Logger logger = LoggerFactory.getLogger(FunctionsFlinkUtil.class);

	public static void putFunctionFilesInFlinkCache(ExecutionEnvironment env, StreamExecutionEnvironment senv, String... functionDescriptionLocations) throws FnOException {
		//if (!FunctionsFlinkUtil.functionDescriptionLocations.isEmpty()) {
		//	logger.warn("Flink distributed cache already initialised. Not adding any functions!");
		//	return;
		//}

		// initialise a DataTypeConverterProvider
		logger.debug("Initialising DataTypeConverterProvider...");
		final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();
		logger.debug("DataTypeConverterProvider initialised!");

		// if no function description files are given, add the "defaut" ones: IDLab functions & GREL functions.
		if (functionDescriptionLocations.length == 0) {
			logger.debug("No function description locations given, so adding IDLab and GREL functions");
			FunctionsFlinkUtil.functionDescriptionLocations.add("functions_grel.ttl");
			FunctionsFlinkUtil.functionDescriptionLocations.add("grel_java_mapping.ttl");
			FunctionsFlinkUtil.functionDescriptionLocations.add("fno/functions_idlab.ttl");
			FunctionsFlinkUtil.functionDescriptionLocations.add("fno/functions_idlab_classes_java_mapping.ttl");
		} else {
			logger.debug("Adding given function description locations.");
			Collections.addAll(FunctionsFlinkUtil.functionDescriptionLocations, functionDescriptionLocations);
		}

		// put function description files in cache
		for (String functionDescriptionLocation : FunctionsFlinkUtil.functionDescriptionLocations) {
			// try to locate the actual file, e.g. when it is on the class path...
			String fileLocationPath = getRealFilePath(functionDescriptionLocation);
			env.registerCachedFile(fileLocationPath, functionDescriptionLocation);
			senv.registerCachedFile(fileLocationPath, functionDescriptionLocation);
		}

		// parse all FnO documents and get the external jar locations, if any.
		logger.debug("Initialising FunctionModelProvider...");
		FunctionModelProvider functionModelProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, functionDescriptionLocations);
		logger.debug("FunctionModelProvider initialised!");
		functionModelProvider.getFunctions().values().stream()
				.map(function -> function.getFunctionMapping().getImplementation().getLocation())
				.filter(location -> !(location).isEmpty())
				.sorted()
				.distinct()
				.forEach(location -> {
					try {
						String realLocation = getRealFilePath(location);
						implementationLocations.add(location);
						env.registerCachedFile(realLocation, location);
						senv.registerCachedFile(realLocation, location);
					} catch (Throwable t) {
						logger.warn("Could not locate '{}'. Certain functions might not execute.", location);
					}
				});

	}

	/**
	 * Tries to instantiate a function Agent from function description locations and implemenation locations known to this
	 * singleton. If the locations are mapped in the given distributed cache, those will be used to initialise the function
	 * Agent.
	 * @param distributedCache	The given Flink distributed cache. If the locations are known to the cache, these are used
	 *                          to initialise the function Agent
	 * @return                  A function agent ready to be used in a RichMapFunction.
	 * @throws FnOException     When something goes wrong
	 */
	public static Agent initFunctionAgentForProcessor(final DistributedCache distributedCache) throws FnOException {
		// map functionDescriptionLocations to cached locations
		String[] mappedFunctionDescriptionLocations = new String[functionDescriptionLocations.size()];
		int i = 0;
		for (String functionDescriptionLocation : functionDescriptionLocations) {
			mappedFunctionDescriptionLocations[i++] = getMappedLocationFromCache(functionDescriptionLocation, distributedCache);
		}

		// map implementation locations
		Map<String, String> implementationLocationsMap = new HashMap<>();
		for (String implementationLocation : implementationLocations) {
			implementationLocationsMap.put(implementationLocation, getMappedLocationFromCache(implementationLocation, distributedCache));
		}

		return AgentFactory.createFromFnO(implementationLocationsMap, mappedFunctionDescriptionLocations);
	}

	/**
	 * This method tries to find the real location of a given file path on the file system.
	 * If the file is not located directly on the system (e.g. in a Jar file or a web URI), then it is copied to
	 * a temporary file then *that* location is returned, so that Flink can opy it into its cache.
	 * @param path The original location path
	 * @return	The location of the contents of the given path. This can be to a temporary file.
	 */
	private static String getRealFilePath(final String path) {
		logger.debug("Finding real location of '{}'...", path);
		URL fileURI = FileFinder.findFile(path);
		if (fileURI == null) {
			String msg = "Could not find location '" + path + "'.";
			logger.warn(msg);
			throw new RuntimeException(msg);
		}
		if (fileURI.getProtocol().equals("file")) {
			String realPath = fileURI.getPath();
			logger.debug("Found a real file at '{}'", realPath);
			return realPath;
		} else {
			// write contents of file to temp file and return that location
			logger.debug("Path '{}' not a real file on the file system. Copying it to a temporary file.", fileURI);
			try	(InputStream in = fileURI.openStream()) {
				File tempFile = File.createTempFile("rmlStreamer", "tmp");
				logger.debug("Created temporary file '{}' to put '{}'", tempFile.getCanonicalPath(), path);
				Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				tempFile.deleteOnExit();
				return tempFile.getCanonicalPath();
			} catch (IOException e) {
				logger.warn("Could not read content of '{}'. This might lead to missing functions.", path);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Returns the location as known in the distributed cache, or the given location if not found in the distributed cache
	 * @param location 	The location to search for in the distributed cache.
	 * @param distributedCache	a Flink distributed cache instance
	 * @return	The mapped location or the given location if not present in cache.
	 */
	private static String getMappedLocationFromCache(final String location, final DistributedCache distributedCache) {
		String mappedLocation;
		try {
			mappedLocation = distributedCache.getFile(location).getAbsolutePath();
		} catch (IllegalArgumentException e) {
			logger.warn("File '{}' not found in file mapping cache. Using this location.", location);
			mappedLocation = location;
		}
		return mappedLocation;
	}
}
