package gpw.pl.tsi.db.entities;

public class StockEntity {

    private Long id;
    private String name;
    private String code;
    private String shortName;
    private String marketId;

    public StockEntity(Long id, String name, String code, String shortName, String marketId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.shortName = shortName;
        this.marketId = marketId;
    }

    public StockEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }
}
