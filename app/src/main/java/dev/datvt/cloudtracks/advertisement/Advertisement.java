package dev.datvt.cloudtracks.advertisement;

/**
 * Created by datvt on 8/9/2016.
 */
public class Advertisement {

    private int logo;
    private String name;
    private String body;
    private float rate;
    private String packageName;

    public Advertisement(int logo, String name, String body, float rate, String packageName) {
        this.logo = logo;
        this.name = name;
        this.body = body;
        this.rate = rate;
        this.packageName  = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
