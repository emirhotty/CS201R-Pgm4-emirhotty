
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // PRE: Input files exist in the project folder
    // POST: Reads media, processes commands, and creates output files
    public static void main(String args[]) {

        System.out.println("Welcome to Media Library");

        ArrayList<Media> media = new ArrayList<>();

        try (
            PrintWriter report = new PrintWriter("report.txt");
            PrintWriter error = new PrintWriter("error.txt")
        ) {
            readDataFile(media, error);
            readCommandFile(media, report, error);

            System.out.println("Program completed.");
            System.out.println("Check report.txt and error.txt.");

        } catch (FileNotFoundException e) {
            System.out.println("Error opening file: " + e.getMessage());
        }
    }

    // PRE: Accepts a record, media list, and error file
    // POST: Adds valid media to the list or writes an error
    public static void newContent(String line,
                                  ArrayList<Media> media,
                                  PrintWriter error) {

        String[] data = line.split(",", -1);

        if (data.length < 7) {
            error.println("Incomplete record: " + line);
            return;
        }

        String typeString = data[0].trim().toUpperCase();

        if (typeString.length() != 1 ||
            "MBS".indexOf(typeString.charAt(0)) == -1) {
            error.println("Invalid type: " + line);
            return;
        }

        char type = typeString.charAt(0);
        String title = data[1].trim();
        String name = data[2].trim();
        String genre = data[4].trim();

        int rating, length, year;

        try {
            rating = Integer.parseInt(data[3].trim());
            length = Integer.parseInt(data[5].trim());
            year = Integer.parseInt(data[6].trim());
        } catch (NumberFormatException e) {
            error.println("Invalid numeric field: " + line);
            return;
        }

        if (rating < 1 || rating > 10) {
            error.println("Invalid rating: " + line);
            return;
        }

        if (length <= 0) {
            error.println("Invalid length: " + line);
            return;
        }

        if (year < 1920 || year > 2024) {
            error.println("Invalid year: " + line);
            return;
        }

        Media item = new Media(type, title, name,
                               rating, genre, length, year);

        media.add(item);

        if (type == 'M')
            Media.mediaCounts[0]++;
        else if (type == 'B')
            Media.mediaCounts[1]++;
        else
            Media.mediaCounts[2]++;
    }

    // PRE: mediaList.txt exists
    // POST: Reads valid records into the ArrayList
    public static void readDataFile(ArrayList<Media> media,
                                    PrintWriter error)
                                    throws FileNotFoundException {

        Scanner input = new Scanner(new File("mediaList.txt"));

        while (input.hasNextLine()) {
            String line = input.nextLine().trim();

            if (line.equalsIgnoreCase("Q"))
                break;

            if (!line.isEmpty())
                newContent(line, media, error);
        }

        input.close();
    }

    // PRE: Accepts an output file
    // POST: Prints the number of movies, books, and songs
    public static void printTotals(PrintWriter report) {

        report.println("\nYOUR MEDIA LIBRARY");
        report.println("Movies: " + Media.mediaCounts[0]);
        report.println("Books: " + Media.mediaCounts[1]);
        report.println("Songs: " + Media.mediaCounts[2]);

        int total = Media.mediaCounts[0]
                  + Media.mediaCounts[1]
                  + Media.mediaCounts[2];

        report.println("TOTAL: " + total);
        report.println();
    }

    // PRE: Accepts media list and optional filters
    // POST: Prints matching media to the report
    public static void printMedia(ArrayList<Media> media,
                                  PrintWriter report,
                                  char type,
                                  int minRating,
                                  String genre) {

        int count = 0;

        report.println("\nMEDIA LIST");
        report.printf("%-37s%10s%10s     %-23s%7s%n",
                      "Title", "Year", "Rating", "Genre", "Type");

        report.println("------------------------------------------------"
                     + "--------------------------------------------");

        for (Media item : media) {

            boolean typeMatch = type == 'A'
                             || item.getType() == type;

            boolean ratingMatch = item.getRating() >= minRating;

            boolean genreMatch = genre == null
                    || item.getGenre().equalsIgnoreCase(genre);

            if (typeMatch && ratingMatch && genreMatch) {
                report.print(item.printMedia());
                count++;
            }
        }

        if (count == 0)
            report.println("No matching media found.");

        report.println("Records found: " + count);
        report.println();
    }

    // PRE: Accepts media list and report
    // POST: Prints all media
    public static void printAll(ArrayList<Media> media,
                                PrintWriter report) {
        printMedia(media, report, 'A', 1, null);
    }

    // PRE: Accepts media list and report
    // POST: Prints all movies
    public static void printMovies(ArrayList<Media> media,
                                   PrintWriter report) {
        printMedia(media, report, 'M', 1, null);
    }

    // PRE: Accepts media list and report
    // POST: Prints all books
    public static void printBooks(ArrayList<Media> media,
                                  PrintWriter report) {
        printMedia(media, report, 'B', 1, null);
    }

    // PRE: Accepts media list and report
    // POST: Prints all songs
    public static void printSongs(ArrayList<Media> media,
                                  PrintWriter report) {
        printMedia(media, report, 'S', 1, null);
    }

    // PRE: mediaCommands.txt exists
    // POST: Executes commands and writes report/error output
    public static void readCommandFile(ArrayList<Media> media,
                                       PrintWriter report,
                                       PrintWriter error)
                                       throws FileNotFoundException {

        Scanner input = new Scanner(new File("mediaCommands.txt"));

        while (input.hasNextLine()) {

            String line = input.nextLine().trim();

            if (line.isEmpty())
                continue;

            String[] parts = line.split(",", -1);
            String command = parts[0].trim().toUpperCase();

            if (command.equals("Q"))
                break;

            if (command.equals("N")) {

                if (parts.length < 8) {
                    error.println("Invalid new media: " + line);
                } else {
                    newContent(
                        line.substring(line.indexOf(',') + 1),
                        media, error
                    );
                }

            } else if (command.equals("T")) {

                if (parts.length == 1)
                    printTotals(report);
                else
                    error.println("Invalid command: " + line);

            } else if (command.equals("A") ||
                       command.equals("M") ||
                       command.equals("B") ||
                       command.equals("S")) {

                char type = command.charAt(0);

                if (parts.length == 1) {

                    if (type == 'A')
                        printAll(media, report);
                    else if (type == 'M')
                        printMovies(media, report);
                    else if (type == 'B')
                        printBooks(media, report);
                    else
                        printSongs(media, report);

                } else if (parts.length == 2) {

                    String value = parts[1].trim();

                    try {
                        int rating = Integer.parseInt(value);

                        if (rating < 1 || rating > 10)
                            error.println(
                                "Error in rating provided: " + rating
                            );
                        else
                            printMedia(media, report, type, rating, null);

                    } catch (NumberFormatException e) {
                        printMedia(media, report, type, 1, value);
                    }

                } else {
                    error.println("Invalid command: " + line);
                }

            } else {
                error.println("Invalid command: " + command);
            }
        }

        input.close();
    }
}
