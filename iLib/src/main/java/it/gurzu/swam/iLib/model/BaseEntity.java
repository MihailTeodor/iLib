package it.gurzu.swam.iLib.model;

import java.util.Objects;

public abstract class BaseEntity {
	
	private Long id;
	private String uuid;
	
	protected BaseEntity() {}
	
	public BaseEntity(String uuid) {
		if(uuid == null) {
			throw new IllegalArgumentException("uuid cannot be null!");
		}
		this.uuid = uuid;
	}

	public Long getId() {
		return id;
	}

	public String getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return Objects.equals(uuid, ((BaseEntity)obj).getUuid());
	}

	
	
}