public class ManufacturerCar {
    private final String model;
    private final String engineType;
    private final int year;
    private final String vin;

    public ManufacturerCar(String model, String engineType, int year, String vin) {
        this.model = model;
        this.engineType = engineType;
        this.year = year;
        this.vin = vin;
    }

    public String getModel() {
        return model;
    }

    public String getEngineType() {
        return engineType;
    }

    public int getYear() {
        return year;
    }

    public String getVin() {
        return vin;
    }

    public void recall(String reason) {
        System.out.println("召回原因：" + reason);
    }

    public void printInfo() {
        System.out.println("车型: " + model + ", 发动机类型: " + engineType + ", 年份: " + year + ", VIN: " + vin);
    }
}
