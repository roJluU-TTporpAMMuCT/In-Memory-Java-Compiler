package org.negro.compiler;

import javax.tools.SimpleJavaFileObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

class CompiledCode extends SimpleJavaFileObject {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private String className;

    public CompiledCode(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
        this.className = className;
    }

    @Override
    public OutputStream openOutputStream() { return baos; }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }
    
    public String getClassName() { return className; }
}
