package com.sw.router;

import java.util.Map;

public interface IRouteGroup {
    void loadInto(Map<String, RouteMeta> routes);
}
