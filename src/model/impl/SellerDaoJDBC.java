package model.impl;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDaoJDBC implements SellerDao {

    private Connection conn;

    public SellerDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Seller obj) {
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO seller "
                + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
                + "VALUES "
                + "(?, ?, ?, ?, ?)")) {

            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new Date(obj.getBirthDate().getTime()));
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());

            st.executeUpdate();
            System.out.println("[MySQL] A ação foi bem sucedida!");


        } catch(SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    @Override
    public void update(Seller obj) {
        try (PreparedStatement st = conn.prepareStatement("UPDATE seller "
                + "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
                + "WHERE Id = ?")) {

            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new Date(obj.getBirthDate().getTime()));
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());
            st.setInt(6, obj.getId());

            int affectedRows = st.executeUpdate();
            System.out.println("[MySQL] A ação de mudança de usuário foi concluída!");
            System.out.println("[MySQL] Linhas afetadas: " + affectedRows);

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    @Override
    public void deleteById(int id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM seller "
        + "WHERE Id = ?")) {

            st.setInt(1, id);

            st.executeUpdate();

            System.out.println("[MySQL] A remoção de mudança de usuário foi concluída!");
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    @Override
    public Seller findById(int id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT seller.*,department.Name as DepName "
                + "FROM seller INNER JOIN department "
                + "ON seller.DepartmentId = department.Id "
                + "WHERE seller.Id = ?")) {

            st.setInt(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Department dept = instantiateDepartment(st);
                    return instantiateSeller(st, dept);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    private Seller instantiateSeller(PreparedStatement st, Department dept) throws SQLException {
        Seller vendedor = new Seller();
        try (ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                vendedor.setId(rs.getInt("Id"));
                vendedor.setName(rs.getString("Name"));
                vendedor.setEmail(rs.getString("Email"));
                vendedor.setBirthDate(rs.getDate("BirthDate"));
                vendedor.setBaseSalary(rs.getDouble("BaseSalary"));
                vendedor.setDepartment(dept);
                return vendedor;
            }
            return vendedor;
        }
    }

    private Department instantiateDepartment(PreparedStatement st) throws SQLException {
        Department dept = new Department();
        try (ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                dept.setId(rs.getInt("DepartmentId"));
                dept.setName(rs.getString("DepName"));
                return dept;
            }
            return dept;
        }
    }

    private Department departmentFinalizable(ResultSet rs) throws SQLException {
        Department dep = new Department();
        dep.setId(rs.getInt("DepartmentId"));
        dep.setName(rs.getString("DepName"));
        return dep;
    }

    private Seller sellerFinalizable(ResultSet rs, Department dept) throws SQLException {
        Seller obj = new Seller();
        obj.setId(rs.getInt("Id"));
        obj.setName(rs.getString("Name"));
        obj.setEmail(rs.getString("Email"));
        obj.setBaseSalary(rs.getDouble("BaseSalary"));
        obj.setBirthDate(rs.getDate("BirthDate"));
        obj.setDepartment(dept);
        return obj;
    }

    @Override
    public List<Seller> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;

        List<Seller> vendedores = new ArrayList<>();
        Map<Integer, Department> depart_registrados = new HashMap<>();

        try {
            st = conn.prepareStatement("SELECT seller.*,department.Name as DepName " +
                    "FROM seller INNER JOIN department " +
                    "ON seller.DepartmentId = department.Id " +
                    "ORDER BY Name");

            rs = st.executeQuery();

            while (rs.next()) {
                Department dep = depart_registrados.get(rs.getInt("DepartmentId"));

                if (dep == null) {
                    dep = departmentFinalizable(rs);
                    depart_registrados.put(rs.getInt("DepartmentId"), dep);
                }
                vendedores.add(sellerFinalizable(rs, dep));
            }
            return vendedores;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeResultSets(rs);
            DB.closeStatments(st);
        }
    }


    @Override
    public List<Seller> findByDepartment(Department department) {
        try (PreparedStatement st = conn.prepareStatement("SELECT seller.*,department.Name as DepName"
                + "SELECT seller.*, department.Name as DepName "
                + "ON seller.DepartmentId = department.Id "
                + "WHERE DepartmentId = ?"
                + "ORDER BY Name")) {

            st.setInt(1, department.getId());

            try (ResultSet rs = st.executeQuery()) {

                List<Seller> vendedores = new ArrayList<>();
                Map<Integer, Department> depart_registrados = new HashMap<>();

                while (rs.next()) {

                    Department dep = depart_registrados.get(rs.getInt("DepartmentId"));

                    if (dep == null) {
                        dep = instantiateDepartment(st);
                        depart_registrados.put(rs.getInt("DepartmentId"), dep);
                    }

                    Seller vendedor = instantiateSeller(st, dep);
                    vendedores.add(vendedor);
                }
                return vendedores;
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }
}
