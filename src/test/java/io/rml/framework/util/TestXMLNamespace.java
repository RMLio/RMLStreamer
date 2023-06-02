package io.rml.framework.util;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
 **/
public class TestXMLNamespace {
	@Test
	public void testNamespacesFromRoot() throws IOException, XMLStreamException {
		String xmlDoc =
				"<?xml version=\"1.0\" encoding=\"utf-16\"?>\n" +
				"<RINFData xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
				"  <MemberStateCode Code=\"BE\" Version=\"1.5\" />\n" +
				"</RINFData>";

		try (InputStream in = new ByteArrayInputStream(xmlDoc.getBytes(StandardCharsets.UTF_16))) {
			Map<String, String> namespaces = XMLNamespace.namespacesOfRoot(in);
			assertEquals("http://www.w3.org/2001/XMLSchema-instance", namespaces.get("xsi"));
			assertEquals("http://www.w3.org/2001/XMLSchema", namespaces.get("xsd"));
		}
	}
}
