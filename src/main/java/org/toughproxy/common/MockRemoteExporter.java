package org.toughproxy.common;

import org.springframework.remoting.support.RemoteExporter;

public class MockRemoteExporter extends RemoteExporter {

    public MockRemoteExporter() {
        super();
    }

    @Override
    public void setService(Object service) {
    }

    @Override
    public Object getService() {
        return "MockRemoteExporter";
    }

    @Override
    public void setServiceInterface(Class<?> serviceInterface) {
    }

    @Override
    public void setRegisterTraceInterceptor(boolean registerTraceInterceptor) {
    }

    @Override
    public void setInterceptors(Object[] interceptors) {
    }

    @Override
    protected void checkService() throws IllegalArgumentException {
    }

    @Override
    protected void checkServiceInterface() throws IllegalArgumentException {
    }
    @Override
    protected String getExporterName() {
        return null;
    }
}
