package it.gurzu.SWAM.iLib.dtoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dto.ArticleDTO;
import it.gurzu.swam.iLib.dto.ArticleMapper;
import it.gurzu.swam.iLib.dto.ArticleType;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;

public class ArticleDTOTest {

    @Test
    public void testToDTOWithBook() {
        Book book = ModelFactory.book();
        book.setTitle("title");
        book.setAuthor("author");
        book.setIsbn("isbn");
        book.setGenre("genre");
        book.setPublisher("publisher");
        book.setLocation("location");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        book.setYearEdition(yearEdition);
        book.setDescription("description");
        book.setState(ArticleState.AVAILABLE);
        
        LocalDate loanDueDate = LocalDate.of(2024, 2, 1);
        LocalDate bookingEndDate = LocalDate.of(2024, 3, 1);
        
        ArticleDTO dto = ArticleMapper.toDTO(book, loanDueDate, bookingEndDate);
        
        assertNotNull(dto);
        assertEquals("title", dto.getTitle());
        assertEquals("author", dto.getAuthor());
        assertEquals("isbn", dto.getIsbn());
        assertEquals(ArticleType.BOOK, dto.getType());
        assertEquals("genre", dto.getGenre());
        assertEquals("publisher", dto.getPublisher());
        assertEquals("location", dto.getLocation());
        assertEquals(yearEdition, dto.getYearEdition());
        assertEquals("description", dto.getDescription());
        assertEquals(ArticleState.AVAILABLE, dto.getState());
        assertEquals(loanDueDate, dto.getLoanDueDate());
        assertEquals(bookingEndDate, dto.getBookingEndDate());
    }

    @Test
    public void testToDTOWithMagazine() {
        Magazine magazine = ModelFactory.magazine();
        magazine.setTitle("title");
        magazine.setIssueNumber(1);
        magazine.setIssn("issn");
        magazine.setGenre("genre");
        magazine.setPublisher("publisher");
        magazine.setLocation("location");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        magazine.setYearEdition(yearEdition);
        magazine.setDescription("description");
        magazine.setState(ArticleState.AVAILABLE);

        LocalDate loanDueDate = LocalDate.of(2024, 2, 1);
        LocalDate bookingEndDate = LocalDate.of(2024, 3, 1);

        ArticleDTO dto = ArticleMapper.toDTO(magazine, loanDueDate, bookingEndDate);
        
        assertNotNull(dto);
        assertEquals(Integer.valueOf(1), dto.getIssueNumber());
        assertEquals("issn", dto.getIssn());
        assertEquals("title", dto.getTitle());
        assertEquals(ArticleType.MAGAZINE, dto.getType());
        assertEquals("genre", dto.getGenre());
        assertEquals("publisher", dto.getPublisher());
        assertEquals("location", dto.getLocation());
        assertEquals(yearEdition, dto.getYearEdition());
        assertEquals("description", dto.getDescription());
        assertEquals(ArticleState.AVAILABLE, dto.getState());
        assertEquals(loanDueDate, dto.getLoanDueDate());
        assertEquals(bookingEndDate, dto.getBookingEndDate());
    }

    @Test
    public void testToDTOWithMovieDVD() {
        MovieDVD movieDVD = ModelFactory.movieDVD();
        movieDVD.setTitle("title");
        movieDVD.setDirector("director");
        movieDVD.setIsan("isan");
        movieDVD.setGenre("genre");
        movieDVD.setPublisher("publisher");
        movieDVD.setLocation("location");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        movieDVD.setYearEdition(yearEdition);
        movieDVD.setDescription("description");
        movieDVD.setState(ArticleState.AVAILABLE);

        LocalDate loanDueDate = LocalDate.of(2024, 2, 1);
        LocalDate bookingEndDate = LocalDate.of(2024, 3, 1);

        ArticleDTO dto = ArticleMapper.toDTO(movieDVD, loanDueDate, bookingEndDate);

        assertNotNull(dto);
        assertEquals("director", dto.getDirector());
        assertEquals("isan", dto.getIsan());
        assertEquals("title", dto.getTitle());
        assertEquals(ArticleType.MOVIEDVD, dto.getType());
        assertEquals("genre", dto.getGenre());
        assertEquals("publisher", dto.getPublisher());
        assertEquals("location", dto.getLocation());
        assertEquals(yearEdition, dto.getYearEdition());
        assertEquals("description", dto.getDescription());
        assertEquals(ArticleState.AVAILABLE, dto.getState());
        assertEquals(loanDueDate, dto.getLoanDueDate());
        assertEquals(bookingEndDate, dto.getBookingEndDate());
    }

    @Test
    public void testToEntity_WithInvalidType_ThrowsIllegalArgumentException() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        dto.setType(null);
        
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			mapper.toEntity(dto);
			
		});
		assertEquals("Article type is required", thrownException.getMessage());	
    }
    
    @Test
    public void testToEntity_WithoutIdentifier_ThrowsIllegalArgumentException() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        dto.setType(ArticleType.BOOK);
        
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			mapper.toEntity(dto);
			
		});
		assertEquals("Article identifier is required", thrownException.getMessage());	
    }

    @Test
    public void testToEntity_WithIncompleteBookInfo_ThrowsIllegalArgumentException() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        dto.setType(ArticleType.BOOK);
        dto.setIsbn("isbn");

		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			mapper.toEntity(dto);
		});
		assertEquals("Author is required", thrownException.getMessage());	
    }

    @Test
    public void testToEntity_WithIncompleteMagazineInfo_ThrowsIllegalArgumentException() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        dto.setType(ArticleType.MAGAZINE);
        dto.setIssn("issn");

		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			mapper.toEntity(dto);
		});
		assertEquals("Issue Number is required", thrownException.getMessage());	
    }

    @Test
    public void testToEntity_WithIncompleteMovieDVDInfo_ThrowsIllegalArgumentException() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        dto.setType(ArticleType.MOVIEDVD);
        dto.setIsan("isan");

		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			mapper.toEntity(dto);
		});
		assertEquals("Director is required", thrownException.getMessage());	
    }
    
    @Test
    public void testToEntity_WithCompleteBookInfo_CreatesBookArticle() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        
        dto.setId(1L);
        dto.setType(ArticleType.BOOK);
        dto.setLocation("location");
        dto.setTitle("title");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        dto.setYearEdition(yearEdition);
        dto.setPublisher("publisher");
        dto.setGenre("genre");
        dto.setDescription("description");
        dto.setState(ArticleState.AVAILABLE);
        dto.setAuthor("author");
        dto.setIsbn("isbn");
        
        Article book = mapper.toEntity(dto);
        
        assertTrue(book instanceof Book);
        assertEquals(dto.getTitle(), book.getTitle());
        assertEquals(dto.getIsbn(), ((Book) book).getIsbn());
        assertEquals(dto.getAuthor(), ((Book) book).getAuthor());
        assertEquals(dto.getGenre(), book.getGenre());
        assertEquals(dto.getPublisher(), book.getPublisher());
        assertEquals(dto.getLocation(), book.getLocation());
        assertEquals(dto.getYearEdition(), book.getYearEdition());
        assertEquals(dto.getDescription(), book.getDescription());
//        assertEquals(dto.getState(), book.getState());
    }

    @Test
    public void testToEntity_WithCompleteMagazineInfo_CreatesMagazineArticle() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        
        dto.setId(1L);
        dto.setType(ArticleType.MAGAZINE);
        dto.setLocation("location");
        dto.setTitle("title");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        dto.setYearEdition(yearEdition);
        dto.setPublisher("publisher");
        dto.setGenre("genre");
        dto.setDescription("description");
        dto.setState(ArticleState.AVAILABLE);
        dto.setIssueNumber(Integer.valueOf(1));
        dto.setIssn("issn");
        
        Article magazine = mapper.toEntity(dto);
        
        assertTrue(magazine instanceof Magazine);
        assertEquals(dto.getTitle(), magazine.getTitle());
        assertEquals(dto.getIssn(), ((Magazine) magazine).getIssn());
        assertEquals(dto.getIssueNumber(), ((Magazine) magazine).getIssueNumber());
        assertEquals(dto.getGenre(), magazine.getGenre());
        assertEquals(dto.getPublisher(), magazine.getPublisher());
        assertEquals(dto.getLocation(), magazine.getLocation());
        assertEquals(dto.getYearEdition(), magazine.getYearEdition());
        assertEquals(dto.getDescription(), magazine.getDescription());
//        assertEquals(dto.getState(), book.getState());
    }

    @Test
    public void testToEntity_WithCompleteMovieDVDInfo_CreatesMovieDVDArticle() {
    	ArticleMapper mapper = new ArticleMapper();
        ArticleDTO dto = new ArticleDTO();
        
        dto.setId(1L);
        dto.setType(ArticleType.MOVIEDVD);
        dto.setLocation("location");
        dto.setTitle("title");
        LocalDate yearEdition = LocalDate.of(2024, 1, 1);
        dto.setYearEdition(yearEdition);
        dto.setPublisher("publisher");
        dto.setGenre("genre");
        dto.setDescription("description");
        dto.setState(ArticleState.AVAILABLE);
        dto.setDirector("director");
        dto.setIsan("isan");
        
        Article movieDVD = mapper.toEntity(dto);
        
        assertTrue(movieDVD instanceof MovieDVD);
        assertEquals(dto.getTitle(), movieDVD.getTitle());
        assertEquals(dto.getIsan(), ((MovieDVD) movieDVD).getIsan());
        assertEquals(dto.getDirector(), ((MovieDVD) movieDVD).getDirector());
        assertEquals(dto.getGenre(), movieDVD.getGenre());
        assertEquals(dto.getPublisher(), movieDVD.getPublisher());
        assertEquals(dto.getLocation(), movieDVD.getLocation());
        assertEquals(dto.getYearEdition(), movieDVD.getYearEdition());
        assertEquals(dto.getDescription(), movieDVD.getDescription());
//        assertEquals(dto.getState(), book.getState());
    }

}
