package Practice;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class TreeNode {
    String name;
    int lockBy;
    AtomicInteger lockedval;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> bottom;

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        lockedval = new AtomicInteger(0);
        childs = new ArrayList<>();
        bottom = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

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
        nttm = new ConcurrentHashMap<>();
    }

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

    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;
        }

        r.lockedval.incrementAndGet();
        if (r.lockedval.get() > 1 || !r.bottom.isEmpty()) {
            r.lockedval.decrementAndGet();
            return false;
        }

        TreeNode par = r.parent;
        while (par != null) {
            par.bottom.add(r);

            if (par.lockedval.get() > 0 || par.isLoc.get()) {
                par.bottom.remove(r);
                r.lockedval.decrementAndGet();
                TreeNode par1 = r.parent;
                while (par1 != par) {
                    par1.bottom.remove(r);
                    par1 = par1.parent;
                }
                return false;
            }
            par = par.parent;
        }

        r.lockBy = id;
        return true;
    }

    boolean unlock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || !r.isLoc.get() || r.lockBy != id) {
            return false;
        }

        TreeNode par = r.parent;
        while (par != null) {
            par.bottom.remove(r);
            par = par.parent;
        }
        r.isLoc.set(false);
        r.lockBy = -1;
        return true;
    }

    boolean upgrade(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || r.isLoc.get() || r.bottom.isEmpty()) {
            return false;
        }

        Set<TreeNode> stt = new HashSet<>(r.bottom);
        for (TreeNode ld : stt) {
            if (ld.lockBy != id) {
                return false;
            }
        }

        for (TreeNode ld : stt) {
            unlock(ld.name, id);
        }
        return lock(name, id);
    }
}

public class Approach3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n, m, q;
        n = sc.nextInt();
        m = sc.nextInt();
        q = sc.nextInt();
        List<String> a = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            a.add(sc.next());
        }
        MTree tree = new MTree(a.get(0));
        tree.makeMTree(a, m);
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
                System.out.println("Thread " + Thread.currentThread().getName() + " Operation: " + ot + " Node " + name + " ID: " + id + " Result : " + result);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        sc.close();
    }
}
