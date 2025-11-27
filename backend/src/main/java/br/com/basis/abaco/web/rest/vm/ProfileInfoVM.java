package br.com.basis.abaco.web.rest.vm;

public final class ProfileInfoVM {

    private String[] activeProfiles;

    private String ribbonEnv;

    private String version;

    public ProfileInfoVM(String[] activeProfiles, String ribbonEnv, String version) {
        this.activeProfiles = activeProfiles.clone();
        this.ribbonEnv = ribbonEnv;
        this.version = version;
    }

    public String[] getActiveProfiles() {
        String[] profileAux;
        profileAux = activeProfiles;
        return profileAux;
    }

    public String getRibbonEnv() {
        return ribbonEnv;
    }

    public String getVersion() {
        return version;
    }
}
