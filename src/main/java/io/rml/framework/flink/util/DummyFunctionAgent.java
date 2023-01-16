package io.rml.framework.flink.util;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This is a "dummy" function agent that just returns the value of the
 * first argument, or null of there are no arguments.
 * <p>
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
public class DummyFunctionAgent implements Agent {
	@Override
	public Object execute(String functionId, Arguments arguments) throws Exception {
		return arguments.size() == 0 ? null : arguments.get(arguments.getArgumentNames().iterator().next());
	}

	@Override
	public Object execute(String s, Arguments arguments, boolean b) throws Exception {
		return execute(s, arguments);
	}

	@Override
	public void close() throws Exception {
		// nothing to close here
	}
}
