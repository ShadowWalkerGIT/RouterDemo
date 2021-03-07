package com.sw.router;

public class RouteMeta {
    private String mDesQualifiedName;
    private Class<?> mDestinationClass;
    private String mGroup;
    private String mPath;

    public RouteMeta() {
    }

    public RouteMeta(String group, String path) {
        mGroup = group;
        mPath = path;
    }

    public RouteMeta(String group, String path, String desQualifiedName) {
        mGroup = group;
        mPath = path;
        mDesQualifiedName = desQualifiedName;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void setDestinationClass(Class<?> clazz) {
        mDestinationClass = clazz;
    }

    public void setDestinationQualifiedName(String qualifiedName) {
        mDesQualifiedName = qualifiedName;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getPath() {
        return mPath;
    }

    public Class<?> getDestinationClass() {
        return mDestinationClass;
    }

    public String getDestinationQualifiedName() {
        return mDesQualifiedName;
    }
}
