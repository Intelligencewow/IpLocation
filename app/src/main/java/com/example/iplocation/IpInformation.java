package com.example.iplocation;

public class IpInformation {

    private String country;
    private String regionName;
    private String city;
    private String isp;
    private String query;
    private String status;
    private float lat;
    private float lon;

    public IpInformation(String country, String region_name, String city, String isp, String query, String status , float lat, float lon) {
        this.country = country;
        this.regionName = region_name;
        this.city = city;
        this.isp = isp;
        this.query = query;
        this.lat = lat;
        this.lon = lon;
        this.status = status;
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

    public String getStatus() {return status;}
}
