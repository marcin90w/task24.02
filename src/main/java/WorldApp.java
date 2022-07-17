import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class WorldApp {

    private Scanner scanner;
    private Connection connection;
    private ArrayList<UpdatedCitiesPopulation> updatedCitiesPopulations = new ArrayList<>();

    public void run() {

        String url = "jdbc:mysql://localhost:3306/world?serverTimeZone=UTC";
        try {
            connection = DriverManager.getConnection(url, "root", "admin");
        } catch (SQLException e) {
            System.out.println("błąd podczas nawiązywania połączenia: " + e.getMessage());
        }

        while(true) {
            printOptions();
            scanner = new Scanner(System.in);
            String option = scanner.nextLine();
            switch (option) {
                case "0" -> {
                    close();
                    return;
                }
                case "1" -> citiesForPoland();
                case "2" -> citiesFromOption();
                case "3" -> languageFromCountryCode();
                case "4" -> {
                    updateCitiesPopulation();
                    printUpdatedCitiesPopulation();
                }
                default -> System.out.println("Undefined option");
            }
        }
    }

    private void updateCitiesPopulation() {
        boolean exit = false;
        while (!exit) {
            uploadCityAndPopulationData();
            System.out.println("Wpisz: true jesli chcesz zakonczyc dodawanie");
            exit = scanner.nextBoolean();
            scanner.nextLine();
            if (exit == true) {
                return;
            }
        }

    }

    private void printUpdatedCitiesPopulation() {
        int updatedRecords = updatedCitiesPopulations.size();
        System.out.println("Zaktualizowane rekordy: " + updatedRecords);
        if (updatedRecords != 0) {
            for (UpdatedCitiesPopulation u : updatedCitiesPopulations) {
                System.out.println("ID: " + u.getId() + " nazwa: " + u.getCity() + ", ludnosc: " + u.getPopulation());
            }
        }
    }

    //Dlaczego mi to nie działa?
    private void uploadCityAndPopulationData() {
        System.out.println("Podaj miasto:");
        String city = scanner.nextLine();
        System.out.println("Podaj nowa liczbe ludnosci:");
        String population = scanner.nextLine();
        int id = 0;
        String sql = "SELECT ID, Name, Population FROM city WHERE Name = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, city);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getInt(1);
            preparedStatement = connection.prepareStatement("UPDATE city SET Population = " + population
                    + " WHERE Name = ?");
            preparedStatement.setString(1,city);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedCitiesPopulations.isEmpty()) {
                updatedCitiesPopulations.add(new UpdatedCitiesPopulation(city, population, id));
            } else if (updatedCitiesPopulationsContainCity(city) > 0) {
                int i = updatedCitiesPopulationsContainCity(city);
                updatedCitiesPopulations.set(i, new UpdatedCitiesPopulation(city, population, id));
            }

        } catch (SQLException e) {
            System.out.println("Brak miasta o podanej nazwie " + city + ". " + e.getMessage());
        }


    }

    private int updatedCitiesPopulationsContainCity(String city) {
        int u = 0;
        for (UpdatedCitiesPopulation updated : updatedCitiesPopulations) {
            if (updated.getCity().equals(city)) {
                u = updatedCitiesPopulations.indexOf(updated);
            }
        }
        return u;
    }

    private void languageFromCountryCode() {
        printAvailableLanguages();
        scanner = new Scanner(System.in);
        String language = scanner.nextLine();
        languageCountries(language);
    }

    private void languageCountries(String language) {
        String sql = "SELECT Name, IsOfficial, Percentage FROM countrylanguage cl " +
                "JOIN country c ON c.code = cl.CountryCode " +
                "WHERE Language = ? " +
                "ORDER BY Percentage DESC";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, language);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String countryName = resultSet.getString("Name");
                String percentage = resultSet.getString("Percentage");
                System.out.println("Język " + language + " w kraju " + countryName+ " jest używany przez " + percentage + "% osób)");
            }
        } catch (SQLException e) {
            System.out.println("Podanego kraju nie ma na liscie! " + e.getMessage());
        }
    }

    private void printAvailableLanguages() {
        String sql = "SELECT DISTINCT Language FROM countrylanguage ORDER BY Language ASC";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void citiesFromOption() {
        System.out.println("Podaj kod kraju z dostępnych kodów:");
        printAvaibleCountryCodes();
        scanner = new Scanner(System.in);
        String countryCode = scanner.nextLine();
        fetchAndDisplayCities(countryCode);
    }

    private void printAvaibleCountryCodes() {
        String sql = "SELECT Code FROM country ORDER BY Code ASC";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void citiesForPoland() {
        fetchAndDisplayCities("POL");
    }

    private void fetchAndDisplayCities(String countryCode) {
        String sql = "SELECT * FROM city WHERE CountryCode = ? ORDER BY Population ASC";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, countryCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cityName = resultSet.getString("Name");
                int population = resultSet.getInt("Population");

                System.out.println(cityName + " - ludność: " + population);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void printOptions() {
        System.out.println("Wybierz jedną z 3 opcji:");
        System.out.println("1 - Wyświetl informacje o Polskich miastach");
        System.out.println("2 - Wyświetl wszystkie miasta z podanego kraju");
        System.out.println("3 - Wyświetl kraje posługujące się podanych językiem");
        System.out.println("4 - Zaktualizuj ludnosc w podanych miastach");
        System.out.println("0 - Koniec");
    }

    private void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Nie można zamknąć połączenia z bazą danych: " + e.getMessage());
        }
    }
}
