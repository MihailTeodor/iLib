package it.gurzu.swam.iLib.model;

public class Subscription extends BaseEntity {
	
	private Article subscribedArticle;
	private User subscriber;

	Subscription() { }
	
	public Subscription(String uuid) {
		super(uuid);
	}
	
	public Article getSubscribedArticle() {
		return subscribedArticle;
	}
	
	public void setSubscribedArticle(Article subscribedArticle) {
		this.subscribedArticle = subscribedArticle;
	}
	
	public User getSubscriber() {
		return subscriber;
	}
	
	public void setSubscriber(User subscriber) {
		this.subscriber = subscriber;
	}
	
	
}