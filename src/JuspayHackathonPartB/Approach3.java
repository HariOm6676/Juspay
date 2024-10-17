package JuspayHackathonPartB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class TreeNode {

    String name;
    int lockBy;
    boolean isLoc;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> locDesc;
    ReentrantLock mutex;

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
        mutex = new ReentrantLock();
    }

    // Add children to this node
    void addChild(List<String> a) {
        for (String nm : a) {
            childs.add(new TreeNode(nm, this));
        }
    }
}

class MTree {
    TreeNode root;
    Map<String, TreeNode> nttm;

    MTree(String name) {
        root = new TreeNode(name, null);
        nttm = new HashMap<>();
    }

    // Create an M-ary tree from a list of names
    void makeMTree(List<String> a, int m) {
        Queue<TreeNode> q = new LinkedList<>();
        int k = 1, i, n = a.size();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode r = q.poll();
            nttm.put(r.name, r);
            List<String> b = new ArrayList<>();
            for (i = k; i < Math.min(n, k + m); i++) {
                b.add(a.get(i));
            }
            r.addChild(b);
            for (TreeNode child : r.childs) {
                q.add(child);
            }
            k = i;
        }
    }

    // Update the locDesc set of all parent nodes
    void updateParents(TreeNode r, TreeNode curr) {
        while (r != null) {
            try
            {
                r.mutex.lock();
                r.locDesc.add(curr);
            }
            finally{
                r.mutex.unlock();
            }
            r = r.parent;
        }
    }

    // Lock a node and its ancestors if possible
    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;
        }

        TreeNode par = r.parent;
        List<ReentrantLock> locks = new ArrayList<>();
        try {
            // Lock ancestors
            while (par != null) {
                par.mutex.lock();
                locks.add(par.mutex);
                par = par.parent;
            }
            // Lock the node itself
            r.mutex.lock();
            locks.add(r.mutex);
            par= r.parent;
            while(par!=null)
            {
                if(par.isLoc)
                {
                    releaseLocks(locks);
                    return false;
                }
                par=par.parent;
            }
            if (r.isLoc || !r.locDesc.isEmpty()) {
                releaseLocks(locks);
                return false;
            }
            // Update node state and lock it
            updateParents(r.parent, r);
            r.isLoc = true;
            r.lockBy = id;
            return true;
        } finally {
            releaseLocks(locks);
        }
    }

    // Release a list of locks in reverse order
    private void releaseLocks(List<ReentrantLock> locks) {
        for (ReentrantLock lock : locks) {
            lock.unlock();
        }
    }

    // Unlock a node if it is locked by the given ID
    boolean unlock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || !r.isLoc || r.lockBy != id) {
            return false;
        }

        List<ReentrantLock> locks = new ArrayList<>();
        try {
            // Lock the node and its ancestors
            TreeNode par = r.parent;
            while (par != null) {
                par.mutex.lock();
                locks.add(par.mutex);
                par = par.parent;
            }
            r.mutex.lock();
            locks.add(r.mutex);

            // Remove the node from its parent’s locDesc set
            par = r.parent;
            while (par != null) {
                par.locDesc.remove(r);
                par = par.parent;
            }
            
            // Update the node state
            r.isLoc = false;
            r.lockBy = -1;
            return true;
        } finally {
            releaseLocks(locks);
        }
    }

    // Upgrade the lock of a node if all its descendants are locked by the same ID
    boolean upgrade(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || r.isLoc || r.locDesc.isEmpty()) {
            return false;
        }

        List<ReentrantLock> locks = new ArrayList<>();
        try {
            // Lock all descendants
            Set<TreeNode> stt = new HashSet<>(r.locDesc);
            for (TreeNode ld : stt) {
                ld.mutex.lock();
                locks.add(ld.mutex);
            }

            // Lock the node itself
            r.mutex.lock();
            locks.add(r.mutex);

            // Check if all descendants are locked by the same ID
            for (TreeNode ld : r.locDesc) {
                if (ld.lockBy != id) {
                    releaseLocks(locks);
                    return false;
                }
            }

            // Unlock descendants and then lock the node
            for (TreeNode ld : stt) {
                unlock(ld.name, id);
            }
            lock(name, id);
            return true;
        } finally {
            releaseLocks(locks);
        }
    }
}

public class Approach3 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n, m, q;
        n = sc.nextInt();
        m = sc.nextInt();
        q = sc.nextInt();
        List<String> a = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            a.add(sc.next());
        }
        MTree tree = new MTree(a.get(0));
        tree.makeMTree(a, m);

        // Define the number of threads in the fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Runnable task = () -> {
            Random rand = new Random();
            for (int i = 0; i < q; i++) {
                int ot = rand.nextInt(3) + 1;
                String name = a.get(rand.nextInt(n));
                int id = rand.nextInt(3);
                boolean result = false;
                switch (ot) {
                    case 1:
                        result = tree.lock(name, id);
                        break;
                    case 2:
                        result = tree.unlock(name, id);
                        break;
                    case 3:
                        result = tree.upgrade(name, id);
                        break;
                }
                System.out.println("Thread " + Thread.currentThread().getName() +
                    " Operation: " + ot + " Node: " + name + " ID: " + id + " Result: " + result);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Submit multiple tasks to the thread pool
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Shut down the executor service
        executor.shutdown();
        sc.close();
    }
}
