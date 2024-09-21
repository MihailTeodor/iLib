package it.gurzu.swam.iLib.dto;

import org.apache.commons.lang3.StringUtils;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;

public class ArticleMapper {

	public static ArticleDTO toDTO(Article article, LoanDTO loanDTO, BookingDTO bookingDTO) {
		if(article == null)
			return null;
		
		ArticleDTO dto = new ArticleDTO();
		dto.setId(article.getId());
		dto.setLocation(article.getLocation());
		dto.setTitle(article.getTitle());
		dto.setYearEdition(article.getYearEdition());
		dto.setPublisher(article.getPublisher());
		dto.setGenre(article.getGenre());
		dto.setDescription(article.getDescription());
		dto.setState(article.getState());
		dto.setBookingDTO(bookingDTO);
		dto.setLoanDTO(loanDTO);
		
		if(article instanceof Book) {
			Book book = (Book) article;
			dto.setType(ArticleType.BOOK);
			dto.setAuthor(book.getAuthor());
			dto.setIsbn(book.getIsbn());
		}else if(article instanceof Magazine) {
			Magazine magazine = (Magazine) article;
			dto.setType(ArticleType.MAGAZINE);
			dto.setIssueNumber(magazine.getIssueNumber());
			dto.setIssn(magazine.getIssn());
		}else if(article instanceof MovieDVD) {
			MovieDVD movieDVD = (MovieDVD) article;
			dto.setType(ArticleType.MOVIEDVD);
			dto.setDirector(movieDVD.getDirector());
			dto.setIsan(movieDVD.getIsan());			
		}
		return dto;
	}

    public Article toEntity(ArticleDTO dto) {
        if (dto.getType() == null)
            throw new IllegalArgumentException("Article type is required");
                
        switch (dto.getType()) {
            case BOOK:
                return createBook(dto);
            case MAGAZINE:
                return createMagazine(dto);
            case MOVIEDVD:
                return createMovieDVD(dto);
            default:
                throw new IllegalArgumentException("Unknown article type: " + dto.getType());
        }
    }

    private Book createBook(ArticleDTO dto) {
    	if(StringUtils.isBlank(dto.getIsbn()))
        	throw new IllegalArgumentException("Article identifier is required");

    	if(StringUtils.isBlank(dto.getAuthor()))
    		throw new IllegalArgumentException("Author is required");
    	
        Book book = ModelFactory.book();
        book.setTitle(dto.getTitle());
        book.setLocation(dto.getLocation());
        book.setYearEdition(dto.getYearEdition());
        book.setPublisher(dto.getPublisher());
        book.setGenre(dto.getGenre());
        book.setDescription(dto.getDescription());
//        book.setState(dto.getState());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        return book;
    }

    private Magazine createMagazine(ArticleDTO dto) {
    	if(StringUtils.isBlank(dto.getIssn()))
        	throw new IllegalArgumentException("Article identifier is required");

    	if(dto.getIssueNumber() == null)
    		throw new IllegalArgumentException("Issue Number is required");
    	
        Magazine magazine = ModelFactory.magazine();
        magazine.setTitle(dto.getTitle());
        magazine.setLocation(dto.getLocation());
        magazine.setYearEdition(dto.getYearEdition());
        magazine.setPublisher(dto.getPublisher());
        magazine.setGenre(dto.getGenre());
        magazine.setDescription(dto.getDescription());
//        magazine.setState(dto.getState());
        magazine.setIssueNumber(dto.getIssueNumber());
        magazine.setIssn(dto.getIssn());
        return magazine;
    }

    private MovieDVD createMovieDVD(ArticleDTO dto) {
    	if(StringUtils.isBlank(dto.getIsan()))
        	throw new IllegalArgumentException("Article identifier is required");

    	if(StringUtils.isBlank(dto.getDirector()))
    		throw new IllegalArgumentException("Director is required");
    	
        MovieDVD movieDVD = ModelFactory.movieDVD();
        movieDVD.setTitle(dto.getTitle());
        movieDVD.setLocation(dto.getLocation());
        movieDVD.setYearEdition(dto.getYearEdition());
        movieDVD.setPublisher(dto.getPublisher());
        movieDVD.setGenre(dto.getGenre());
        movieDVD.setDescription(dto.getDescription());
//        movieDVD.setState(dto.getState());
        movieDVD.setDirector(dto.getDirector());
        movieDVD.setIsan(dto.getIsan());
        return movieDVD;
    }

}
