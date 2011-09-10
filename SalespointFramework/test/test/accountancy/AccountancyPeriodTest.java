package test.accountancy;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.salespointframework.core.accountancy.PersistentAccountancy;
import org.salespointframework.core.accountancy.AbstractAccountancyEntry;
import org.salespointframework.core.accountancy.Accountancy;
import org.salespointframework.core.accountancy.ProductPaymentEntry;
import org.salespointframework.core.accountancy.SomeOtherEntry;
import org.salespointframework.core.database.Database;
import org.salespointframework.core.money.Money;
import org.salespointframework.core.order.OrderIdentifier;
import org.salespointframework.core.users.UserIdentifier;

public class AccountancyPeriodTest {
	private PersistentAccountancy a;

	private DateTime from;
	private DateTime to;

	@BeforeClass
	public static void classSetup() {
		Database.INSTANCE.initializeEntityManagerFactory("SalespointFramework");
	}

	@Before
	public void testSetup() {
		a = new PersistentAccountancy();
		ProductPaymentEntry p;
		System.out.println("Creating AccountancyEntries: ");
		for(int i = 0; i < 20; i++) {
			 p = new ProductPaymentEntry(new OrderIdentifier(), new UserIdentifier(), new Money(1));
			 System.out.println("Adding p " + p + " with time " + p.getTimeStamp());
			 a.addEntry(p);
			 
			 if(i == 5)
				 from = p.getTimeStamp();
			 if(i == 15)
				 to = p.getTimeStamp();
			 
			 try {
				 Thread.sleep(100);
			 } catch(InterruptedException e) {
				 e.printStackTrace();
			 }
		}
	}

	@Test
	public void periodSetTest() {
		System.out.println("Getting entries from " + from + " to " + to);
		Map<Interval, Iterable<ProductPaymentEntry>> m = a.getEntries(ProductPaymentEntry.class, from, to, Period.millis(200));
		for(Entry<Interval, Iterable<ProductPaymentEntry>> e : m.entrySet()) {
			System.out.println("ProductPaymentEntries for interval " + e.getKey());
			for(ProductPaymentEntry p : e.getValue()) {
				System.out.println("\t" + p);				
			}
		}
	}

	@Test
	public void singlePeriodTest() {
		System.out.println("Getting entries from " + from + " to " + to);
		Map<Interval, Iterable<ProductPaymentEntry>> m = a.getEntries(ProductPaymentEntry.class, from, to, new Period(from, to));
		for(Entry<Interval, Iterable<ProductPaymentEntry>> e : m.entrySet()) {
			System.out.println("ProductPaymentEntries for interval " + e.getKey());
			for(ProductPaymentEntry p : e.getValue()) {
				System.out.println("\t" + p);
			}
		}
	}
	
	@Test
	public void periodMoneyTest() {
		Money total;
		System.out.println("Getting entries from " + from + " to " + to);
		Map<Interval, Iterable<ProductPaymentEntry>> m = a.getEntries(ProductPaymentEntry.class, from, to, new Period(200));
		for(Entry<Interval, Iterable<ProductPaymentEntry>> e : m.entrySet()) {
			total = Money.ZERO;
			for(ProductPaymentEntry p : e.getValue()) {
				System.out.println("\t" + p.getValue());
				total = total.add_(p.getValue());
			}
			System.out.println("Money for interval " + e.getKey() + ": " + total);
			
		}
		System.out.println("Getting entries from " + from + " to " + to);
		Map<Interval, Money> sales = a.getSalesVolume(ProductPaymentEntry.class, from, to, new Period(200));
		for(Entry<Interval, Money> e : sales.entrySet()) {
			System.out.println("Money for interval " + e.getKey() + ": " + e.getValue());
		}
	}

}