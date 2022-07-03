package org.negro.compiler;

import javax.tools.SimpleJavaFileObject;

import java.io.IOException;
import java.net.URI;

class SourceCode extends SimpleJavaFileObject {
	private String contents;
	private String className;

	public SourceCode(String className, String contents) throws Exception {
		super(URI.create("string:///" + className.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return contents;
	}
	
	public String getClassName() { return className; }
}