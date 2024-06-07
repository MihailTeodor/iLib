package it.gurzu.swam.iLib.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/v1")
@ApplicationScoped
public class MyApplication extends Application {}
