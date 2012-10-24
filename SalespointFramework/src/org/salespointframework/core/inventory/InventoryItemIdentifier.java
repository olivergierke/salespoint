package org.salespointframework.core.inventory;

import javax.persistence.Embeddable;

import org.salespointframework.util.SalespointIdentifier;

/**
 * 
 * @author Paul Henke
 * 
 */
@SuppressWarnings("serial")
@Embeddable
public class InventoryItemIdentifier extends SalespointIdentifier
{
	/**
	 * Creates a new unique identifier for {@link InventoryItem}s.
	 */
	public InventoryItemIdentifier()
	{
		super();
	}

	/**
	 * Only needed for property editor, shouldn't be used otherwise.
	 * 
	 * @param serialNumber
	 *            The string representation of the identifier.
	 */
	@Deprecated
	public InventoryItemIdentifier(String inventoryItemIdentifier)
	{
		super(inventoryItemIdentifier);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object other) { 
		return super.equals(other);
	}
}