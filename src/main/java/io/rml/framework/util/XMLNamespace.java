package io.rml.framework.util;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (C) 2017 - 2023 RDF Mapping Language (RML)
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
 *
 * @author Gerald Haesendonck
 **/
public class XMLNamespace {
	final static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	public static Map<String, String> namespacesOfRootFromFile(final String path) throws IOException, XMLStreamException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(path))) {
			return namespacesOfRoot(in);
		}
	}

	public static Map<String, String> namespacesOfRoot(final String xmlDoc) throws IOException, XMLStreamException {
		try (InputStream in = new ByteArrayInputStream(xmlDoc.getBytes(StandardCharsets.UTF_16))) {
			return namespacesOfRoot(in);
		}
	}

	public static Map<String, String> namespacesOfRoot(final InputStream in) throws XMLStreamException {
		Map<String, String> prefixToIRI = new HashMap<>(2);
		XMLEventReader reader = null;
		try {
			XMLEventReader xmlReader = xmlInputFactory.createXMLEventReader(in);
			boolean firstElementProcessed = false;
			while (xmlReader.hasNext()) {
				XMLEvent event = xmlReader.nextEvent();
				System.out.println(event.toString());
				if (event.isStartElement()) { // first is the root
					if (firstElementProcessed) {
						// then this is not the first element anymore
						break;
					}
					firstElementProcessed = true;

					// now get namespaces, if any
					StartElement startElement = event.asStartElement();
					for (Iterator<Namespace> it = startElement.getNamespaces(); it.hasNext(); ) {
						Namespace namespace = it.next();
						prefixToIRI.put(namespace.getPrefix(), namespace.getNamespaceURI());
					}
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
					// ignore
				}
			}
		}
		return prefixToIRI;
	}
}
