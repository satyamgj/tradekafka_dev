package com.finmamba.resolver.services;
import java.sql.Timestamp;
import java.util.*;

class Order {
	int order_id;
	double price;
	int quantity;
	Timestamp insertionTime;

	public Order(int order_id, double price, int quantity, Timestamp insertionTime) {
		this.order_id = order_id;
		this.price = Math.round(price * 10000) / 10000.0; // Round the price to 4 decimal places
		this.quantity = quantity;
		this.insertionTime = insertionTime;
	}
}

class LimitOrderBook {
	Map<Double, List<Order>> buyOrders;
	Map<Double, List<Order>> sellOrders;

	public LimitOrderBook() {
		buyOrders = new TreeMap<>(Collections.reverseOrder());
		sellOrders = new TreeMap<>();
	}

	public void submitOrder(Order order) {
		if (order.quantity <= 0) {
			return;
		}

		if (order.price >= 0) {
			buyOrders.computeIfAbsent(order.price, k -> new ArrayList<>()).add(order);
			matchOrders(sellOrders, order);
		} else {
			sellOrders.computeIfAbsent(order.price, k -> new ArrayList<>()).add(order);
			matchOrders(buyOrders, order);
		}
	}

	public void matchOrders(Map<Double, List<Order>> oppositeOrders, Order newOrder) {
		List<Double> oppositePrices = new ArrayList<>(oppositeOrders.keySet());
		List<Order> matchedOrders = new ArrayList<>();

		for (double price : oppositePrices) {
			// if ((newOrder.price >= 0 && newOrder.price >= price) || (newOrder.price < 0
			// && newOrder.price <= price)) {
			List<Order> oppositeOrderList = oppositeOrders.get(price);

			// Iterate over the list using an iterator to safely remove elements
			Iterator<Order> iterator = oppositeOrderList.iterator();
			while (iterator.hasNext() && newOrder.quantity > 0) {
				Order oppositeOrder = iterator.next();
				// if ((newOrder.price >= 0 && oppositeOrder.price < 0) || (newOrder.price < 0
				// && oppositeOrder.price >= 0)) {

				// Check if new Order buy condition
				if (newOrder.quantity > 0 && newOrder.price > 0 && Math.abs(oppositeOrder.price) <= newOrder.price) {
					int quantityMatched = Math.min(newOrder.quantity, oppositeOrder.quantity);
					System.out.println("Matched order " + oppositeOrder.order_id + " with order " + newOrder.order_id
							+ " price: " + newOrder.price + " for " + quantityMatched + " at " + oppositeOrder.price
							+ ((newOrder.price < 0) && Math.abs(newOrder.price) < oppositeOrder.price ? " correct Sell "
									: (newOrder.price > Math.abs(oppositeOrder.price)) ? " correct buy "
											: " incorrect "));
					oppositeOrder.quantity -= quantityMatched;
					newOrder.quantity -= quantityMatched;

					if (oppositeOrder.quantity <= 0) {
						iterator.remove();
						matchedOrders.add(oppositeOrder);
					}

					if (newOrder.quantity <= 0) {

						List<Order> orderList = oppositeOrders.get(newOrder.price);
						if (orderList != null) {
							orderList.remove(newOrder);
							if (orderList.isEmpty()) {
								oppositeOrders.remove(newOrder.price);
							}
						}
						buyOrders.remove(newOrder.price);
						buyOrders.put(newOrder.price, matchedOrders);
						break;

					}

				}
				// Check if new Order is a sell order
				else if (newOrder.quantity > 0 && newOrder.price < 0
						&& oppositeOrder.price >= Math.abs(newOrder.price)) {

					int quantityMatched = Math.min(newOrder.quantity, oppositeOrder.quantity);
					System.out.println("Matched order " + oppositeOrder.order_id + " with order " + newOrder.order_id
							+ " price: " + newOrder.price + " for " + quantityMatched + " at " + oppositeOrder.price
							+ ((newOrder.price < 0) && Math.abs(newOrder.price) < oppositeOrder.price ? " correct Sell "
									: (newOrder.price > Math.abs(oppositeOrder.price)) ? " correct buy "
											: " incorrect "));
					oppositeOrder.quantity -= quantityMatched;
					newOrder.quantity -= quantityMatched;

					if (oppositeOrder.quantity <= 0) {
						iterator.remove();
						matchedOrders.add(oppositeOrder);
					}

					if (newOrder.quantity <= 0) {

						List<Order> orderList = oppositeOrders.get(newOrder.price);
						if (orderList != null) {
							orderList.remove(newOrder);
							if (orderList.isEmpty()) {
								oppositeOrders.remove(newOrder.price);
							}
						}
						sellOrders.remove(newOrder.price);
						sellOrders.put(newOrder.price, matchedOrders);

						break;
					}
				}
				// }
			}

			if (oppositeOrderList.isEmpty()) {
				oppositeOrders.remove(price);
			}

		}
		// }

		for (Order matchedOrder : matchedOrders) {
			oppositePrices.remove(matchedOrder.price);
		}
	}

	public void printOrderBook() {
		System.out.println("Buy Orders:");
		for (double price : buyOrders.keySet()) {
			List<Order> orderList = buyOrders.get(price);
			for (Order order : orderList) {
				System.out.println(
						"Order ID: " + order.order_id + ", Price: " + order.price + ", Quantity: " + order.quantity);
			}
		}

		System.out.println("Sell Orders:");
		for (double price : sellOrders.keySet()) {
			List<Order> orderList = sellOrders.get(price);
			for (Order order : orderList) {
				System.out.println(
						"Order ID: " + order.order_id + ", Price: " + order.price + ", Quantity: " + order.quantity);
			}
		}
	}
}

public class OrderMatching {
	public static void main(String[] args) {
		LimitOrderBook orderBook = new LimitOrderBook();

		// Test Case 1: Submitting buy and sell orders
		System.out.println("Test Case 1: Submitting buy and sell orders");
		orderBook.submitOrder(new Order(1, 99.99, 3, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(2, 99.99, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(3, 95.50, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(4, 95.50, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.printOrderBook();
		System.out.println();

		/*
		 * Expected Output: Test Case 1: Submitting buy and sell orders Buy Orders:
		 * Order ID: 1, Price: 100.1234, Quantity: 5 Order ID: 2, Price: 98.5678,
		 * Quantity: 7
		 * 
		 * Sell Orders: Order ID: 3, Price: -99.9876, Quantity: 3 Order ID: 4, Price:
		 * -97.5432, Quantity: 10
		 */

		// Test Case 2: Submitting multiple orders at the same price
		System.out.println("Test Case 2: Submitting multiple orders at the same price");
		orderBook.submitOrder(new Order(5, -99.999, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(6, -98.5, 2, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(7, -98.5, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(8, -98.65, 1, new Timestamp(System.currentTimeMillis())));
		orderBook.printOrderBook();
		System.out.println();

		/*
		 * Expected Output: Test Case 2: Submitting multiple orders at the same price
		 * Buy Orders: Order ID: 2, Price: 98.5678, Quantity: 7 Order ID: 5, Price:
		 * 98.5678, Quantity: 3 Order ID: 6, Price: 98.5678, Quantity: 4
		 * 
		 * Sell Orders: Order ID: 7, Price: -97.5432, Quantity: 5 Order ID: 8, Price:
		 * -97.5432, Quantity: 7 Order ID: 3, Price: -99.9876, Quantity: 3 Order ID: 4,
		 * Price: -97.5432, Quantity: 10
		 */

		// Test Case 3: Submitting orders with zero quantity
		System.out.println("Test Case 3: Submitting orders with zero quantity");
		orderBook.submitOrder(new Order(9, 99.1234, 0, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(10, -98.5678, 0, new Timestamp(System.currentTimeMillis())));
		orderBook.printOrderBook();
		System.out.println();

		/*
		 * Expected Output: Test Case 3: Submitting orders with zero quantity Buy
		 * Orders: Order ID: 2, Price: 98.5678, Quantity: 7 Order ID: 5, Price: 98.5678,
		 * Quantity: 3 Order ID: 6, Price: 98.5678, Quantity: 4
		 * 
		 * Sell Orders: Order ID: 7, Price: -97.5432, Quantity: 5 Order ID: 8, Price:
		 * -97.5432, Quantity: 7 Order ID: 3, Price: -99.9876, Quantity: 3 Order ID: 4,
		 * Price: -97.5432, Quantity: 10
		 */

		// Test Case 4: Submitting orders with negative price
		System.out.println("Test Case 4: Submitting orders with negative price");
		orderBook.submitOrder(new Order(11, -100.9876, 5, new Timestamp(System.currentTimeMillis())));
		orderBook.submitOrder(new Order(12, -101.5432, 7, new Timestamp(System.currentTimeMillis())));
		orderBook.printOrderBook();
		System.out.println();

		/*
		 * Expected Output: Test Case 4: Submitting orders with negative price Buy
		 * Orders: Order ID: 2, Price: 98.5678, Quantity: 7 Order ID: 5, Price: 98.5678,
		 * Quantity: 3 Order ID: 6, Price: 98.5678, Quantity: 4
		 * 
		 * Sell Orders: Order ID: 12, Price: -101.5432, Quantity: 7 Order ID: 7, Price:
		 * -97.5432, Quantity: 5 Order ID: 11, Price: -100.9876, Quantity: 5 Order ID:
		 * 8, Price: -97.5432, Quantity: 7 Order ID: 3, Price: -99.9876, Quantity: 3
		 * Order ID: 4, Price: -97.5432, Quantity: 10
		 */

		// Test Case 5: Bulk Testing
		System.out.println("Test Case 5: Bulk Testing");
		int numOrders = 10000;
		Random random = new Random();

		// Generate and submit buy orders
		for (int i = 1; i <= numOrders; i++) {
			double price = 90.0 + random.nextDouble() * 20.0;
			int quantity = 1 + random.nextInt(10);
			orderBook.submitOrder(new Order(i, price, quantity, new Timestamp(System.currentTimeMillis())));
		}

		// Generate and submit sell orders
		for (int i = numOrders + 1; i <= 2 * numOrders; i++) {
			double price = -110.0 + random.nextDouble() * 20.0;
			int quantity = 1 + random.nextInt(10);
			orderBook.submitOrder(new Order(i, price, quantity, new Timestamp(System.currentTimeMillis())));
		}

		// Print a portion of the order book
		orderBook.printOrderBook();
		System.out.println();
	}
}
