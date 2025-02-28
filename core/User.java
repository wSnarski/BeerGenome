package core;

import java.util.List;

public abstract class User implements ORMClass {
	private int userId;
	private String username;
	private String password;
	
	/* *********** Abstract methods that need to be implemented. *********** */
	//constructor implementations
	protected abstract void constructorHelper(int userId) throws LoginException;
	
	//Login logic
	protected abstract void constructorHelper(String username, String password, Boolean newAccount) throws LoginException;
	public abstract Boolean authenticate(String username, String password);

	//Stuff about UserAttributeJoin
	public abstract List<AttributeRating> getAllRatedAttributes();
	public abstract void setAttributeRating(Attribute attribute, int newRating);
	public abstract void setAttributeRating(int attributeId, int newRating);
	public abstract int getAttributeRating(Attribute attribute); //TODO-optimize
	
	//Stuff about consumables and recommendations.
	public abstract void setRecommendationRating(Consumable consumable, int newRating);
	public abstract void setRecommendationRating(int consumableId, int newRating);
	public abstract List<Recommendation> getRatedConsumables();
	
	/* *********** IMPLEMENTED METHODS *********** */
	public Recommendation getRecommendation(Consumable c) throws CoreException 
	{
		for(Recommendation r : this.getRatedConsumables())
		{
			if(r.getConsumable().getConsumableId() == c.getConsumableId())
				return r;
		}
		throw new CoreException("That consumable has not beed rated by this user.");
	}
	
	//TODO-nf-feature
	public List<Consumable> getRecommendedConsumables(Consumable anyConsumable)
	{
		List<Consumable> allConsumables = anyConsumable.getAllConsumables();
		
		//Remove all rated consumables from allConsumables
		//TODO will removing during the loop cause problems?
		List<Recommendation> ratedConsumables = this.getRatedConsumables();
		int size =allConsumables.size();
		for(int i = 0 ; i < size; i++)
		{
			for(Recommendation r : ratedConsumables)
			{
				if(r.getConsumable().getConsumableId() == allConsumables.get(i).getConsumableId())
				{
					allConsumables.remove(i);
					size--; //TODO ?
				}
			}
		}
		
		//Sort allConsumables on the items' ratings.
		//Example of how to get the initial rating:
		//Rating r = new Rating(this, allConsumables.get(something));
		//int initialRating = r.getInitialRating();
		
		return allConsumables;
	}
	
	
	/* *********** GETTERS AND SETTERS *********** */
	//getters and setters.
	protected void setUserId(int userId) {
		this.userId = userId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUsernameWithoutCommit(String username) {
		this.username = username;
	}
	public void setUsername(String username)  {
		setUsernameWithoutCommit(username);
		
		try
		{
			commit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public String getUsername() {
		return username;
	}
	public void setPasswordWithoutCommit(String password) {
		this.password = password;
	}
	public void setPassword(String password) {
		setPasswordWithoutCommit(password);
		
		try
		{
			commit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public String getPassword() {
		return password;
	}
	
	public boolean equals(User u)
	{
		if(u.getUserId() == this.getUserId())
		{
			return true;
		}
		else
		{
			return false;
		}
	}


}
