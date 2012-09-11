package models.push;

import java.util.Date;

import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.PreSave;

public abstract class BaseEntity {

	public Date createdAt;
	public Date updatedAt;
	
	@PrePersist
	public void updated() {
		updatedAt = new Date();
	}
	
	@PreSave
	public void created() { 
		createdAt = new Date();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result
				+ ((updatedAt == null) ? 0 : updatedAt.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntity other = (BaseEntity) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		return true;
	}

}
