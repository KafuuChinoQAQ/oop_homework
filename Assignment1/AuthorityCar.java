public class AuthorityCar {
    private final String licensePlate;
    private final String registrationDate;
    private boolean isInspected;
    private boolean isStolen;

    public AuthorityCar(String licensePlate, String registrationDate, boolean isInspected, boolean isStolen) {
        this.licensePlate = licensePlate;
        this.registrationDate = registrationDate;
        this.isInspected = isInspected;
        this.isStolen = isStolen;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public boolean isInspected() {
        return isInspected;
    }

    public boolean isStolen() {
        return isStolen;
    }

    public void inspect() {
        isInspected = true;
        System.out.println("车辆 " + licensePlate + " 已通过年检。");
    }

    public void reportStolen() {
        isStolen = true;
        System.out.println("车辆 " + licensePlate + " 已被报失。");
    }

    public void printInfo() {
        System.out.println("车牌号: " + licensePlate + ", 注册日期: " + registrationDate + ", 年检: " + (isInspected ? "已通过" : "未通过") + ", 是否被盗: " + (isStolen ? "是" : "否"));
    }
}
