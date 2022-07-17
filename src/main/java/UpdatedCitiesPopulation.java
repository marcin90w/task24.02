public class UpdatedCitiesPopulation {

    private String city;
    private String population;
    private int id;

    public UpdatedCitiesPopulation(String city, String population, int id) {
        this.city = city;
        this.population = population;
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
