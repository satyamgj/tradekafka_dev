package com.finmamba.resolver.services;
import java.util.*;

class Order {
    int order_id;
    double price;
    int quantity;

    public Order(int order_id, double price, int quantity) {
        this.order_id = order_id;
        this.price = price;
        this.quantity = quantity;
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
            if ((newOrder.price >= 0 && newOrder.price >= price) || (newOrder.price < 0 && newOrder.price <= price)) {
                List<Order> oppositeOrderList = oppositeOrders.get(price);
                int oppositeQuantity = oppositeOrderList.stream().mapToInt(o -> o.quantity).sum();

                while (!oppositeOrderList.isEmpty() && newOrder.quantity > 0) {
                    Order oppositeOrder = oppositeOrderList.get(0);
                    int quantityMatched = Math.min(newOrder.quantity, oppositeOrder.quantity);
                    System.out.println("Matched order " + oppositeOrder.order_id + " with order " + newOrder.order_id + " for " + quantityMatched + " at " + oppositeOrder.price);

                    oppositeOrder.quantity -= quantityMatched;
                    newOrder.quantity -= quantityMatched;

                    if (oppositeOrder.quantity <= 0) {
                        oppositeOrderList.remove(0);
                        matchedOrders.add(oppositeOrder);
                    } else {
                        break;
                    }
                }

                if (oppositeOrderList.isEmpty()) {
                    oppositeOrders.remove(price);
                }

                if (newOrder.quantity <= 0) {
                    break;
                }
            }
        }

        for (Order matchedOrder : matchedOrders) {
            oppositePrices.remove(matchedOrder.price);
        }
    }

    public void printOrderBook() {
        System.out.println("Buy Orders:");
        for (double price : buyOrders.keySet()) {
            List<Order> orderList = buyOrders.get(price);
            for (Order order : orderList) {
                System.out.println("Order ID: " + order.order_id + ", Price: " + order.price + ", Quantity: " + order.quantity);
            }
        }

        System.out.println("Sell Orders:");
        for (double price : sellOrders.keySet()) {
            List<Order> orderList = sellOrders.get(price);
            for (Order order : orderList) {
                System.out.println("Order ID: " + order.order_id + ", Price: " + order.price + ", Quantity: " + order.quantity);
            }
        }
    }
    
    public class Main {
        public void main(String[] args) {
            LimitOrderBook orderBook = new LimitOrderBook();

            // Submit buy and sell orders
            orderBook.submitOrder(new Order(1, 100, 5));
            orderBook.submitOrder(new Order(2, 98, 7));
            orderBook.submitOrder(new Order(3, -99, 3));
            orderBook.submitOrder(new Order(4, -97, 10));

            // Print the order book
            orderBook.printOrderBook();
        }
    }

}
