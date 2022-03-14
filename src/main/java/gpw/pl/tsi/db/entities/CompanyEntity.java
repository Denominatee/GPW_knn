package gpw.pl.tsi.db.entities;

public class CompanyEntity {

    private String isin;
    private String companyName;

    public CompanyEntity(String isin, String companyName) {
        this.isin = isin;
        this.companyName = companyName;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
