package com.example.iplocation;

public class IpApiResponse {

    private String country;
    private String regionName;
    private String city;
    private String isp;
    private String query;
    private float lat;
    private float lon;

    public IpApiResponse(String country, String region_name, String city, String isp, String query, float lat, float lon) {
        this.country = country;
        this.regionName = region_name;
        this.city = city;
        this.isp = isp;
        this.query = query;
        this.lat = lat;
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getIsp() {
        return isp;
    }

    public String getQuery() {
        return query;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}
