package tech.okcredit.home.ui.supplier_tab;

import org.joda.time.DateTime;

import java.util.Comparator;

import in.okcredit.merchant.suppliercredit.Supplier;

public class SupplierComparator {
    public static class Name implements Comparator<Supplier> {
        @Override
        public int compare(Supplier c1, Supplier c2) {
            return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
        }
    }

    public static class AbsoluteBalance implements Comparator<Supplier> {
        @Override
        public int compare(Supplier c1, Supplier c2) {
            float b1 = Math.abs(c1.getBalance());
            float b2 = Math.abs(c2.getBalance());
            if (b1 > b2) return -1;
            else if (b1 < b2) return 1;
            else return 0;
        }
    }

    public static class RecentActivity implements Comparator<Supplier> {
        @Override
        public int compare(Supplier c1, Supplier c2) {
            DateTime a1 =
                    c1.getLastActivityTime() == null
                            ? c1.getCreateTime()
                            : c1.getLastActivityTime();
            DateTime a2 =
                    c2.getLastActivityTime() == null
                            ? c2.getCreateTime()
                            : c2.getLastActivityTime();

            if (a1.isAfter(a2)) {
                return -1;
            } else if (a1.isBefore(a2)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private SupplierComparator() {}
}
