package com.example.literature.main;

import com.example.literature.model.Author;
import com.example.literature.model.Book;
import com.example.literature.model.BookData;
import com.example.literature.model.Results;
import com.example.literature.repository.AuthorRepository;
import com.example.literature.repository.BookRepository;
import com.example.literature.service.ConsumptionAPI;
import com.example.literature.service.ConvertData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private ConvertData convertData = new ConvertData();
    private ConsumptionAPI consumptionApi = new ConsumptionAPI();
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;

    List<Book> books;
    List<Author> authors;

    public Main(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public void showMenu() {
        final var menu = """
                \n\t**** Please, select an option ****
                \t1 - Search book by title
                \t2 - List registered books
                \t3 - List registered authors
                \t4 - List authors alive in a given year
                \t5 - List books by language
                \n\t0 - Exit
                """;
        var option = -1;
        System.out.println("****************************************");
        while (option != 0) {
            System.out.println(menu);
            System.out.print("Option: ");
            option = sc.nextInt();
            sc.nextLine();
            switch (option) {
                case 1:
                    searchBookByTitle();
                    break;
                case 2:
                    listRegisteredBooks();
                    break;
                case 3:
                    listRegisteredAuthors();
                    break;
                case 4:
                    listAuthorsAliveInYear();
                    break;
                case 5:
                    listBooksByLanguage();
                    break;
                case 0:
                    System.out.println("ending application...");
                    break;
                default:
                    System.out.println("Invalid option, please, try again");
                    break;
            }
        }
        System.out.println("****************************************");
    }

    private void searchBookByTitle() {
        System.out.print("Search book by title...Please, enter title: ");
        String inTitle = sc.nextLine();
        var json = consumptionApi.getData(inTitle.replace(" ", "%20"));
        //System.out.println("json: " + json);
        var data = convertData.getData(json, Results.class);
        //System.out.println("data: " + data);
        if (data.results().isEmpty()) {
            System.out.println("Book not found");
        } else {
            BookData bookData = data.results().getFirst();
            //System.out.println("bookData: " + bookData);
            Book book = new Book(bookData);
            //System.out.println("book: " + book);
            Author author = new Author().getFirstAuthor(bookData);
            //System.out.println("author: " + author);
            saveData(book, author);
        }
    }

    private void saveData(Book book, Author author) {
        Optional<Book> bookFound = bookRepository.findByTitleContains(book.getTitle());
        //System.out.println("bookFound: " + bookFound);
        if (bookFound.isPresent()) {
            System.out.println("this book was already registered");
        } else {
            try {
                bookRepository.save(book);
                System.out.println("book registered");
            } catch (Exception e) {
                System.out.println("Error message: " + e.getMessage());
            }
        }

        Optional<Author> authorFound = authorRepository.findByNameContains(author.getName());
        //System.out.println("authorFound: " + authorFound);
        if (authorFound.isPresent()) {
            System.out.println("this author was already registered");
        } else {
            try {
                authorRepository.save(author);
                System.out.println("author registered");
            } catch (Exception e) {
                System.out.println("Error message: " + e.getMessage());
            }
        }
    }

    private void listRegisteredBooks() {
        System.out.println("List registered books\n---------------------");
        books = bookRepository.findAll();
        books.stream()
                .sorted(Comparator.comparing(Book::getTitle))
                .forEach(System.out::println);
    }

    private void listRegisteredAuthors() {
        System.out.println("List registered authors\n-----------------------");
        authors = authorRepository.findAll();
        authors.stream()
                .sorted(Comparator.comparing(Author::getName))
                .forEach(System.out::println);
    }

    private void listAuthorsAliveInYear() {
        System.out.print("List authors alive in a given year...Please, enter year: ");
        Integer year = Integer.valueOf(sc.nextLine());
        authors = authorRepository
                .findByBirthYearLessThanEqualAndDeathYearGreaterThanEqual(year, year);
        if (authors.isEmpty()) {
            System.out.println("Authors alive not found");
        } else {
            authors.stream()
                    .sorted(Comparator.comparing(Author::getName))
                    .forEach(System.out::println);
        }
    }

    private void listBooksByLanguage() {
        System.out.println("List books by language\n----------------------");
        System.out.println("""
                \n\t---- Please, select a language ----
                \ten - English
                \tes - Spanish
                \tfr - French
                \tpt - Portuguese
                """);
        String lang = sc.nextLine();
        books = bookRepository.findByLanguageContains(lang);
        if (books.isEmpty()) {
            System.out.println("Books by language selected not found");
        } else {
            books.stream()
                    .sorted(Comparator.comparing(Book::getTitle))
                    .forEach(System.out::println);
        }
    }
}
