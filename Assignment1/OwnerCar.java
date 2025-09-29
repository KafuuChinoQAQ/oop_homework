public class OwnerCar {
    private final String licensePlate;
    private final String ownerName;
    private final String color;
    private int mileage;

    public OwnerCar(String licensePlate, String ownerName, String color, int mileage) {
        this.licensePlate = licensePlate;
        this.ownerName = ownerName;
        this.color = color;
        this.mileage = mileage;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getColor() {
        return color;
    }

    public int getMileage() {
        return mileage;
    }

    public void drive(int distance) {
        mileage += distance;
        System.out.println("已行驶: " + distance + " 公里，总里程: " + mileage);
    }

    public void printInfo() {
        System.out.println("车牌号: " + licensePlate + ", 车主: " + ownerName + ", 颜色: " + color + ", 里程: " + mileage);
    }
}
