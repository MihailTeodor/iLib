package it.gurzu.swam.iLib.dto;

import java.util.List;

public class PaginationResponse<T> {
    private List<T> items;
    private int pageNumber;
    private int resultsPerPage;
    private long totalResults;
    private int totalPages;

    public PaginationResponse(List<T> items, int pageNumber, int resultsPerPage, long totalResults, int totalPages) {
        this.items = items;
        this.pageNumber = pageNumber;
        this.resultsPerPage = resultsPerPage;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getResultsPerPage() {
		return resultsPerPage;
	}

	public void setResultsPerPage(int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	public long getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(long totalResults) {
		this.totalResults = totalResults;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

}

