package server;

import java.util.ArrayList;
import java.util.List;

import core.Attribute;
import core.AttributeRating;
import core.Consumable;
import core.LoginException;
import core.Recommendation;
import core.User;
import database.DBAbstraction;
import database.DBAbstractionException;
import database.SQLDatabase;
import database.SQLUserAttribute;

public class ServerUser extends User {
	private DBAbstraction database;
	
	public ServerUser(int userId) {
		constructorHelper(userId);
	}
	
	@Override
	protected void constructorHelper(int userId) {
		setUserId(userId);
		try {
			refresh();
		}
		catch(DBAbstractionException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param newAccount
	 * @throws Exception login exception.
	 */
	public ServerUser(String username, String password, Boolean newAccount) throws LoginException
	{
		constructorHelper(username, password, newAccount);
	}
	
	@Override
	protected void constructorHelper(String username, String password, Boolean newAccount) throws LoginException {
		if(newAccount)
		{
			try {
				setUserId(getDatabase().addUser(username, password));
				refresh();
			} catch (DBAbstractionException e) {
				throw new LoginException(e);
			}
		}
		else
		{
			try {
				setUserId(getDatabase().getIdFromUsername(username));
				refresh();
			} catch (DBAbstractionException e) {
				throw new LoginException(e);
			}
			
			if(!authenticate(username, password))
			{
				setUserId(0);
				throw new LoginException("Invalid username/password.");
			}
		}
	}
	
	
	@Override
	public Boolean authenticate(String username, String password) {
		try
		{
			if(getUserId() == 0)
			{
				return false;
			}
			if(username.equals(getDatabase().getUsername(getUserId())) &&
					password.equals(getDatabase().getPassword(getUserId())))
			{
				return true;
			}
		}
		catch(DBAbstractionException e)
		{
			return false;
		}
		return false;
	}

	@Override
	public void commit() throws DBAbstractionException {
		getDatabase().setUsername(getUserId(), getUsername());
		getDatabase().setPassword(getUserId(), getPassword());
	}

	@Override
	public void refresh() throws DBAbstractionException {
		setUsernameWithoutCommit(getDatabase().getUsername(getUserId()));
		setPasswordWithoutCommit(getDatabase().getPassword(getUserId()));
	}

	public void setDatabase(DBAbstraction database) {
		this.database = database;
	}

	public DBAbstraction getDatabase() {
		if(database == null)
		{
			database = SQLDatabase.getInstance();
		}
		return database;
	}

	@Override
	public void setAttributeRating(Attribute attribute, int newRating) {
		setAttributeRating(attribute.getAttributeId(), newRating);
	}
	
	@Override
	public int getAttributeRating(Attribute attribute)
	{
		for(AttributeRating r : this.getAllRatedAttributes())
		{
			if(r.getAttribute().getAttributeId() == attribute.getAttributeId())
			{
				return r.getRating();
			}
		}
		
		return -1;
	}

	@Override
	public void setAttributeRating(int attributeId, int newRating) {
		try
		{
			SQLUserAttribute.getInstance().addAtrributeRating(getUserId(), attributeId, newRating);
		}
		//Note: This exception is triggered if the userid,attributeid combination is not unique. If that is
		//the case, we want to edit the attribute rating instead of adding it.
		catch(DBAbstractionException e)
		{
			System.err.println("runQuery error mentioned above happened in setAttributeRating and is " +
					"handled. See ServerUser.setAttributeRating(int,int).");
			try
			{
				SQLUserAttribute.getInstance().editAtrributeRating(getUserId(), attributeId, newRating);
			}
			//And this is a read database problem.
			catch(DBAbstractionException realException)
			{
				System.err.println("There are two errors -- one with edit and one with add. Shown in that order.");
				realException.printStackTrace();
				e.printStackTrace();
			}
		}		
	}

	@Override
	public List<AttributeRating> getAllRatedAttributes() {
		try {
			return SQLDatabase.getInstance().getAllRatedAttributes(getUserId());
		} catch (DBAbstractionException e) {
			e.printStackTrace();
		}
		
		List<AttributeRating> list = new ArrayList<AttributeRating>();
		return list;
	}
	
	@Override
	public List<Recommendation> getRatedConsumables() {
		try {
			return SQLDatabase.getInstance().getAllConsumablesRated(getUserId());
		} catch (DBAbstractionException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<Recommendation>();
	}

	

	@Override
	public void setRecommendationRating(Consumable consumable, int newRating) {
		setRecommendationRating(consumable.getConsumableId(), newRating);
	}

	@Override
	public void setRecommendationRating(int consumableId, int newRating) {
		Consumable consumable = new ServerConsumable(consumableId);
		
		boolean set = false;
		for(Recommendation r : getRatedConsumables())
		{
			if(r.getConsumable().getConsumableId() == consumableId)
			{
				r.setRevisedRating(newRating);
				set = true;
				break;
			}
		}
		
		if(!set)
		{
			Recommendation r = new ServerRecommendation(this, consumable, newRating);
		}		
	}

	//TODO - UNIMPLEMENTED.
	@Override
	public List<Consumable> getRecommendedConsumables() {
		// TODO Auto-generated method stub
		//TODO-nf get all consumables and order them by their ratings.
		return null;
	}
}
