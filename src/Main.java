import db.DB;
import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        SellerDao sellerDao = DaoFactory.createSellerDao();

        Department department = new Department(5, null);
        Seller pessoa = new Seller(8, "Alex Borgariun", "alexborguns@gmail.com", new Date(), 6700, department);

        sellerDao.insert(pessoa);
        DB.closeConnection();
    }
}