package org.negro.compiler;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.*;

import javax.tools.*;


public class InMemoryJavaCompiler {
	
	private JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Instrumentation inst;
	ExtendedStandardJavaFileManager fileManager;
	
	private Iterable<String> options;
	boolean ignoreWarnings = false;

	private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();
	
	public InMemoryJavaCompiler(Instrumentation inst) {
		javac = ToolProvider.getSystemJavaCompiler();
		this.inst = inst;
		classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader() );
		fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
	}
	
	public InMemoryJavaCompiler(JavaCompiler javac, ClassLoader classLoader, 
			Instrumentation inst, StandardJavaFileManager fileManager) {
		this.javac = javac;
		this.classLoader = new DynamicClassLoader(classLoader);
		this.fileManager = new ExtendedStandardJavaFileManager(fileManager, this.classLoader);
		this.inst = inst;
	}

	public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoader(parent);
		return this;
	}
	
	public InMemoryJavaCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}
	
	public InMemoryJavaCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}
	
	public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
		sourceCodes.put(className, new SourceCode(className, sourceCode));
		return this;
	}
	
	public DynamicClassLoader getDynamicClassLoader() { return classLoader; }
	
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}
	
	public Map<String, Class<?>> compileAll() throws Exception {
		if (sourceCodes.size() == 0)
			throw new CompilationException("No source code to compile");
		
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, sourceCodes.values() );
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) 
			diagnoseResult(collector.getDiagnostics());

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (String className : sourceCodes.keySet()) {
			Class<?> cl = classLoader.loadClass(className);
			inst.redefineClasses(new ClassDefinition(cl, classLoader.getByteCode(className) ) );
			classes.put(className, cl);
		}
		sourceCodes.clear();
		return classes;
	}
	
	public void diagnoseResult(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		StringBuffer exceptionMsg = new StringBuffer("Unable to compile the source");
		boolean throwException = false;
		for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
			switch (d.getKind()) {
			case NOTE:
			case MANDATORY_WARNING:
			case WARNING:
				throwException = !ignoreWarnings;
				break;
			case OTHER:
			case ERROR:
			default:
				throwException = true;
				break;
			}
			exceptionMsg.append("\n").append("[kind=").append(d.getKind());
			exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
			exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
		}
		if (throwException)
			throw new CompilationException(exceptionMsg.toString());
	}
	
}
	

