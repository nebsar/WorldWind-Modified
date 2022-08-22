/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deneme;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author EagleEye
 */
public class ConcurrencyExample {

    public static void main(String[] args) {
        try {
            Callable<Integer> task = () -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    return 123;
                } catch (InterruptedException e) {
                    throw new IllegalStateException("task interrupted", e);
                }
            };

            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<Integer> future = executor.submit(task);

            System.out.println("future done? " + future.isDone());

            Integer result = future.get();

            System.out.println("future done? " + future.isDone());
            
            System.out.print("result: " + result);
        } catch (InterruptedException ex) {
            Logger.getLogger(ConcurrencyExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ConcurrencyExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
