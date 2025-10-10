public class EmploymentInfo {
    private String employerName;
    private String companyAddress;
    private String jobTitle;
    private double salary;

    public EmploymentInfo(String employerName, String companyAddress, String jobTitle, double salary) {
        this.employerName = employerName;
        this.companyAddress = companyAddress;
        this.jobTitle = jobTitle;
        this.salary = salary;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }



}
