package com.robo.onlinebudget.repository;


import com.robo.onlinebudget.entity.SpendsEntity;
import com.robo.onlinebudget.entity.SpendsMonthlyEntity;
import com.robo.onlinebudget.entity.WagesEntity;
import com.robo.onlinebudget.form.SaveNewMonth;
import com.robo.onlinebudget.form.SaveNewWage;
import com.robo.onlinebudget.form.SaveSpends;
import com.robo.onlinebudget.model.Month;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
@SuppressWarnings("unchecked")
public class MonthlySpendsDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public MonthlySpendsDAO() {
    }

    //  METHODS OF MONTHLY WAGES
    public List getPaymentTemplate(boolean isInactive) {

        String hql = "FROM "
                + SpendsEntity.class.getName()
                + " WHERE inactive = "
                + isInactive;

        List<SpendsEntity> spendsEntityList = sessionFactory.getCurrentSession()
                .createQuery(hql)
                .list();

        List<Map> map = new ArrayList<>();

        for (int i = 0; i < spendsEntityList.size(); i++) {
            SpendsEntity spendsEntity = spendsEntityList.get(i);
            Map<String, String> submap = new LinkedHashMap<>();

            submap.put("id", String.valueOf(spendsEntity.getId()));
            submap.put("name", spendsEntity.getName());
            submap.put("amount", String.valueOf(spendsEntity.getAmount()));
            submap.put("salaryPrepaid", String.valueOf(spendsEntity.getSalaryPrepaid()));
            submap.put("withdraw", String.valueOf(spendsEntity.getWithdraw()));
            submap.put("index", String.valueOf(spendsEntity.getIndex()));
            submap.put("inactive", String.valueOf(spendsEntity.getInactive()));

            map.add(i, submap);
        }

        return map;
    }

    public void savePaymentTemplate(List<SaveSpends> savePaymentTMP) {
        savePaymentTMP.forEach(payment -> {
            SpendsEntity se = new SpendsEntity(payment.getId(),
                    payment.getName(),
                    payment.getAmount(),
                    payment.getSalaryPrepaid(),
                    payment.getWithdraw(),
                    payment.getIndex());
            sessionFactory.getCurrentSession().update(se);
        });
    }

    public void addNewSpendToTemplate(SaveSpends editTMPSpends) throws Exception {

        if (!Optional.ofNullable(editTMPSpends.getName())
                .filter(name -> !name.isEmpty())
                .isPresent()) {
            throw new Exception("Name of spend must not be empty");
        }

        if (!Optional.ofNullable(editTMPSpends.getAmount())
                .isPresent()) {
            throw new Exception("Amount of spend must not be empty");
        }

        String hql = "SELECT new "
                + SpendsEntity.class.getName()
                + "(e.index) FROM "
                + SpendsEntity.class.getName()
                + " e "
                + "ORDER BY e.index DESC";
        SpendsEntity indexMaxNum = sessionFactory.getCurrentSession()
                .createQuery(hql, SpendsEntity.class)
                .setMaxResults(1)
                .uniqueResult();

        SpendsEntity se = new SpendsEntity(editTMPSpends.getId(),
                editTMPSpends.getName(),
                editTMPSpends.getAmount(),
                editTMPSpends.getSalaryPrepaid(),
                editTMPSpends.getWithdraw(),
                indexMaxNum.getIndex() + 1);

        sessionFactory.getCurrentSession().save(se);

        if (editTMPSpends.getApplyToCurrentMonth()) addSpendToMonth(se.getId());

    }

    public void addSpendToMonth(Long id) {
            SpendsMonthlyEntity sme = new SpendsMonthlyEntity();
            List<Month> month = getNLastMonth(1);
            sme.setDate(month.get(0).getDate());
            sme.setSpendId(id);
            sme.setAmount(0);
            sessionFactory.getCurrentSession().persist(sme);
    }

    public void deleteSpendFromTemplate(Long id) {
        String hql = "FROM "
                + SpendsEntity.class.getName()
                + " WHERE id = :id";

        SpendsEntity seResult = (SpendsEntity) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("id", id)
                .getSingleResult();

        seResult.setInactive(true);

        sessionFactory.getCurrentSession().update(seResult);

        Month month = (Month) getNLastMonth(1).stream()
                .findAny()
                .orElse(null);

        String smeDate = month.getDate();
        String hq2 = "DELETE "
                + SpendsMonthlyEntity.class.getName()
                + " WHERE spendId = :spendId AND date = :date";

        Query q2 = sessionFactory.getCurrentSession()
                .createQuery(hq2)
                .setParameter("spendId", id)
                .setParameter("date", smeDate);

        q2.executeUpdate();
    }

    public void restoreSpend(Long id) {
        String hql = "FROM "
                + SpendsEntity.class.getName()
                + " WHERE id = :id";

        Query q = sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("id", id);

        SpendsEntity se = (SpendsEntity) q.getSingleResult();
        se.setInactive(false);
        sessionFactory.getCurrentSession().update(se);

//        SpendsMonthlyEntity sme = new SpendsMonthlyEntity();
//
//        Month month = (Month) getNLastMonth(1).stream()
//                .findAny()
//                .orElse(null);
//
//        sme.setDate(month.getDate());
//        sme.setSpendId(se.getId());
//        sme.setAmount(0);
//        sessionFactory.getCurrentSession().persist(sme);
    }

    public List getSpendsNames(int n) {
        List<Map> result = new ArrayList<>();

        if (n > 1){
            List<List<Month>> monthList = getNLastMonth(n);

            monthList.forEach(list -> {
                Map<String, String> submap = new LinkedHashMap<>();

                list.forEach(month -> submap.put(String.valueOf(month.getId()), String.valueOf(month.getName())));

                submap.put("date", list.get(0).getDate());
                result.add(submap);
            });
        } else if (n == 1){
            Map<String, String> submap = new LinkedHashMap<>();
            List<Month> monthList = getNLastMonth(n);

            monthList.forEach(month -> submap.put(String.valueOf(month.getId()), String.valueOf(month.getName())));

            submap.put("date", monthList.get(0).getDate());
            result.add(submap);
        } else {
            System.err.println("GODDAMN WHAT'S WRONG WITH YOUR NUMBER OF MONTHS");
        }

        return result;
    }

    public String checkBeforeCreateNewMonth() {

        List<Month> spendsMonthlyList = getNLastMonth(1);

        LocalDate ldIncoming = LocalDate.now();
        LocalDate ldDB = LocalDate.parse(spendsMonthlyList.get(0).getDate());

        boolean sameDay = ldIncoming.getYear() == ldDB.getYear() && ldIncoming.getMonth() == ldDB.getMonth(); // check for the current date already exist

        if (sameDay) {
            return "The current month is not over yet!";
        } else {
            for (Month m : spendsMonthlyList) {
                if (m.getAmount() < m.getMonthAmount()) return "The current month not all payments are paid off!";
                else {
                    createNewMonth();
                    return null;
                }
            }
        }
        return "Unknown creating months error.";
    }

    @SuppressWarnings("unchecked")
    public void createNewMonth() { // ADD NEW PAYMENT MONTH

        List<SpendsEntity> sne = sessionFactory.getCurrentSession()
                .createQuery("FROM "
                + SpendsEntity.class.getName()
                + " WHERE inactive = 0")
                .getResultList();

        sne.forEach(spendsEntity -> {
            SpendsMonthlyEntity sme = new SpendsMonthlyEntity();
            sme.setDate(String.valueOf(LocalDate.now()));
            sme.setSpendId(spendsEntity.getId());
            sme.setAmount(0);
            sessionFactory.getCurrentSession().persist(sme);
        });

    }

    public List getAllMonths() {

        String hql = "SELECT new "
                + SpendsMonthlyEntity.class.getName()
                + " (s.date) FROM "
                + SpendsMonthlyEntity.class.getName()
                + " AS s";

        List<SpendsMonthlyEntity> smeList = sessionFactory.getCurrentSession()
                .createQuery(hql, SpendsMonthlyEntity.class)
                .getResultList();

        List<String> datesList = new ArrayList<>();

        smeList.forEach(sme -> datesList.add(sme.getDate()));

        List<String> uniqueDatesList = datesList.stream()
                .distinct()
                .collect(Collectors.toList());

        List<List> resultList = new ArrayList<>();

        uniqueDatesList.forEach(uniqueDate -> {
            LocalDate date = LocalDate.parse(uniqueDate);
            try {
                resultList.add(getMonthByDate(date, false));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Collections.reverse(resultList);

        return resultList;

    }

@SuppressWarnings("unchecked")
    public List getNLastMonth(int numberOfMonths) {
        List<Month> spendsMonthlyList;
        List<List> resultList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        Integer tryCounts = 0;

            for (int i = 0; i < numberOfMonths; i++) {
                spendsMonthlyList = getMonthByDate(now);

                while (spendsMonthlyList.isEmpty() && tryCounts < 12) {
                    if (now.getMonthValue() > 1) {
                        now = now.minusMonths(1);
                        spendsMonthlyList = getMonthByDate(now);
                    } else {
                        Calendar cal = Calendar.getInstance();
                        now = LocalDate.of(cal.get(Calendar.YEAR)-1,12, 1);
                        spendsMonthlyList = getMonthByDate(now);
                    }
                    tryCounts++;
                }
                now = now.minusMonths(1);
                if (numberOfMonths == 1) return spendsMonthlyList;
                else resultList.add(spendsMonthlyList);
            }
            return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<Month> getMonthByDate(LocalDate d, boolean... activeOnly) {

        String hql = "FROM "
                + SpendsEntity.class.getName()
                + " AS se INNER JOIN se.spendsMonthly"
                + " WHERE month(date) = :month"
                + " and"
                + " year(date) = :year";

        hql = !Optional.ofNullable(activeOnly).isPresent() ? hql.concat(" and se.inactive = 0") : hql;

        SpendsEntity se;
        SpendsMonthlyEntity sme;
        List<Month> result = new ArrayList<>();

        Query query = sessionFactory
                .getCurrentSession()
                .createQuery(hql);

        query.setParameter("month", d.getMonth().getValue());
        query.setParameter("year", d.getYear());

        List<Object[]> spendsMonthlyList = query.list();

        for (int i = 0; i < spendsMonthlyList.size(); i++) {
            Object[] row = spendsMonthlyList.get(i);
            se = (SpendsEntity) row[0];
            sme = (SpendsMonthlyEntity) row[1];
            Month m = new Month();

            m.setId(sme.getId());
            m.setSpendId(sme.getSpendId());
            m.setName(se.getName());
            m.setAmount(se.getAmount());
            m.setMonthAmount(sme.getAmount());
            m.setIndex(se.getIndex());
            m.setDate(sme.getDate());
            m.setSalaryPrepaid(se.getSalaryPrepaid());
            m.setWithdraw(se.getWithdraw());
            m.setInactive(se.getInactive());
            result.add(m);
        }

        return result;
    }

    public void saveExistingMonth(List<SaveNewMonth> saveNewMonthMap) {

        saveNewMonthMap.forEach( saveNewMonth -> {
            String hql = "FROM "
                    + SpendsMonthlyEntity.class.getName()
                    + " AS sme WHERE id = "
                    + saveNewMonth.getId();
            SpendsMonthlyEntity sme = sessionFactory.getCurrentSession()
                    .createQuery(hql, SpendsMonthlyEntity.class)
                    .getSingleResult();

            if (saveNewMonth.getAmount() == null) saveNewMonth.setAmount(0);

            sme.setAmount(saveNewMonth.getAmount());
            sessionFactory.getCurrentSession().update(sme);
        });

    }

////  SALARY AND PREPAID METHODS

    public List<WagesEntity> getAllWages() {
        String hql = "SELECT new "
                + WagesEntity.class.getName()
                + "(e.id, e.salaryDate, e.salary, e.prepaidDate, e.prepaid) FROM "
                + WagesEntity.class.getName()
                + " e "
                + "ORDER BY e.id DESC";

        return sessionFactory.getCurrentSession()
                .createQuery(hql, WagesEntity.class)
                .getResultList();

    }

    public WagesEntity getLastWage() {
        String hql = "SELECT new "
                + WagesEntity.class.getName()
                + "(e.id, e.salaryDate, e.salary, e.prepaidDate, e.prepaid) FROM "
                + WagesEntity.class.getName()
                + " e "
                + "ORDER BY e.id DESC";

        WagesEntity wagesEntity = sessionFactory.getCurrentSession()
                .createQuery(hql, WagesEntity.class)
                .setMaxResults(1)
                .uniqueResult();

        return wagesEntity;

    }

    public void saveNewWage(SaveNewWage saveNewWage) {
        WagesEntity we = new WagesEntity();

        we.setSalaryDate(saveNewWage.getSalaryDate());
        we.setSalary(saveNewWage.getSalary());
        we.setPrepaidDate(saveNewWage.getPrepaidDate());
        we.setPrepaid(saveNewWage.getPrepaid());

        sessionFactory.getCurrentSession().saveOrUpdate(we);

    }

    public void editExistSalary(SaveNewWage saveNewWage) {

        String hql = "FROM "
                + WagesEntity.class.getName()
                + " AS we WHERE id = "
                + saveNewWage.getId();

        WagesEntity we = sessionFactory.getCurrentSession()
                .createQuery(hql, WagesEntity.class)
                .getSingleResult();

        we.setSalaryDate(saveNewWage.getSalaryDate());
        we.setSalary(saveNewWage.getSalary());
        we.setPrepaidDate(saveNewWage.getPrepaidDate());
        we.setPrepaid(saveNewWage.getPrepaid());

        sessionFactory.getCurrentSession().update(we);

    }

    public void delSalary(Long id) {
        String hql = "DELETE "
                + WagesEntity.class.getName()
                + " WHERE id = :id";

        Query q = sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("id", id);

        q.executeUpdate();
    }

    // ADMIN FEATURES

//    public void delLastMonth() {
//        List<SpendsMonthlyEntity> list = getNLastMonth(1);
//        SpendsMonthlyEntity sme = list.get(0);
//
//        String hql = "DELETE " + SpendsMonthlyEntity.class.getName() + " WHERE date = :date";
//        Query q = sessionFactory.getCurrentSession().createQuery(hql).setParameter("date", sme.getDate());
//        q.executeUpdate();
//    }

}
