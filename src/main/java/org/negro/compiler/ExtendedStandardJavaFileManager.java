package org.negro.compiler;


import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private DynamicClassLoader cl;
	
	public ExtendedStandardJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
		super(fileManager);
		this.cl = cl;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(
			JavaFileManager.Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) {

		try {
			CompiledCode innerClass = new CompiledCode(className);
			cl.addCode(innerClass);
			return innerClass;
		} catch (Exception e) {
			throw new RuntimeException("Error while creating in-memory output file for " + className, e);
		}
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location) { return cl; }
}
