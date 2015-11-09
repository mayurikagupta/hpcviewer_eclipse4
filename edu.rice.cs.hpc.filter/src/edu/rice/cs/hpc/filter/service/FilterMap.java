package edu.rice.cs.hpc.filter.service;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.common.util.AliasMap;
import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.data.filter.IFilterData;

/******************************************************************
 * 
 * Map to filter a scope either exclusively on inclusively
 * @see FilterAttribute
 *
 ******************************************************************/
public class FilterMap extends AliasMap<String, FilterAttribute> 
implements IFilterData
{

	static private final String FILE_NAME = "filter.map";
	//static private final FilterMap filterMap = new FilterMap();
	
	private FilterStateProvider filterStateProvider = null;
	
	public FilterMap() {
		checkData();
	}
	
	/****
	 * Factory for generating a filter map
	 * @return FilterMap : a filter map
	 */
	public static FilterMap getInstance()
	{
		return new FilterMap();
	}
	
	public int size() 
	{
		if (data != null) {
			return data.size();
		}
		return 0;
	}
	
	/***
	 * retrieve the iterator of the hash map
	 * @return
	 */
	public Iterator<Entry<String, FilterAttribute>> iterator() {
		checkData();
		return data.entrySet().iterator();
	}
	
	/****
	 * Check if two FilterMap have the same key and values
	 * @param other
	 * @return
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof FilterMap) {
			FilterMap otherMap = (FilterMap) other;
			if (data.size() == otherMap.data.size()) {
				Set<Entry<String, FilterAttribute>> set1 = otherMap.data.entrySet();
				Set<Entry<String, FilterAttribute>> set2 = data.entrySet();
				return set1.equals(set2);
			}
		}
		return false;
	}
	
	@Override
	public String getFilename() {
		
		IPath path = Platform.getLocation().makeAbsolute();
		return path.append(FILE_NAME).makeAbsolute().toString();
	}

	@Override
	public void initDefault() {
	}
	
	/******
	 * retrieve a list of filters
	 * 
	 * @return
	 */
	public Object[] getEntrySet() {
		checkData();
		return data.entrySet().toArray();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.AliasMap#put(java.lang.Object, java.lang.Object)
	 */
	public void put(String filter, FilterAttribute state)
	{
		super.put(filter, state);
		//save();
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#select(java.lang.String)
	 */
	public boolean select(String element)
	{
		return getFilterAttribute(element) != null;
	}
	
	/*****
	 * Check whether a string can be filtered or not.
	 * If the string match a filter, returns the filter attribute
	 * Otherwise returns null;
	 * 
	 * <p>See {@link edu.rice.cs.hpc.data.filter.FilterAttribute}
	 * </p>
	 * 
	 * @param element
	 * @return {@link FilterAttribute} 
	 * if the element matches to a filter pattern. Return null otherwise.
	 */
	@Override
	public FilterAttribute getFilterAttribute(String element) 
	{
		if (!isFilterEnabled())
		{
			return null;
		}
		Object []entries = getEntrySet();
		
		// --------------------------------------------------------------------------------
		// this is a bad bad bad practice.
		// the complexity is O(NM) where N is the number of nodes and M is the number of patterns
		// we know this is not quick, but assuming M is very small, the order should be linear
		// --------------------------------------------------------------------------------
		for (Object entry : entries)
		{
			Entry<String, FilterAttribute> pattern = (Entry<String, FilterAttribute>) entry;
			FilterAttribute toFilter = pattern.getValue();
			if (toFilter.enable)
			{
				final String key = pattern.getKey().replace("*", ".*").replace("?", ".?");
				if (element.matches(key)) {
					return toFilter;
				}
			}
		}
		return null;
	}
	
	/*****
	 * rename the filter
	 * 
	 * @param oldKey : old filter
	 * @param newKey : new filter
	 * 
	 * @return true if the update successful, false otherwise
	 */
	public boolean update(String oldKey, String newKey)
	{
		FilterAttribute val = get(oldKey);
		return update(oldKey, newKey, val);
	}

	public boolean update(String oldKey, String newKey, FilterAttribute attribute)
	{
		FilterAttribute val = get(oldKey);
		if (val != null)
		{
			remove(oldKey);
			put(newKey, attribute);
			//save();
			return true;
		}
		return false;
	}
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#isFilterEnabled()
	 */
	public boolean isFilterEnabled() 
	{
		if (filterStateProvider == null)
		{
			IWorkbenchWindow window = Util.getActiveWindow();
			Assert.isNotNull(window);
			ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
			filterStateProvider   = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
		}
		
		return filterStateProvider.isEnabled();
	}
}
