package edu.iua.nexus;

public final class Constants {
    public static final String URL_API = "/api";
	public static final String URL_API_VERSION = "/v1";
	public static final String URL_BASE = URL_API + URL_API_VERSION;

	public static final String URL_MAIL = URL_BASE + "/mail";	
    // Exteral Auth services
    public static final String URL_EXTERNAL_LOGIN = URL_BASE + "/login";

    // Internal Auth services
    public static final String URL_AUTH = URL_BASE + "/auth";
    public static final String URL_INTERNAL_LOGIN = URL_AUTH + "/login";
    public static final String URL_TOKEN_VALIDATE = URL_AUTH + "/validate";

    public static final String URL_DETAILS = URL_BASE + "/details";
    public static final String URL_PRODUCTS = URL_BASE + "/products";
    public static final String URL_ORDERS = URL_BASE + "/orders";
    public static final String URL_SUPPLIERS = URL_BASE + "/suppliers";
    public static final String URL_USERS = URL_BASE + "/users";

    public static final String URL_INTEGRATION = URL_BASE + "/integration";
    public static final String URL_INTEGRATION_CLI1 = URL_INTEGRATION + "/cli1";
    public static final String URL_INTEGRATION_CLI2 = URL_INTEGRATION + "/cli2";
    public static final String URL_INTEGRATION_CLI3 = URL_INTEGRATION + "/cli3";
    public static final String URL_ALARMS = URL_BASE + "/alarms";
}
