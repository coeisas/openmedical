/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.fachadas;

import java.util.List;
import javax.ejb.Stateless;
import modelo.entidades.FacManualTarifario;

/**
 *
 * @author santos
 */
@Stateless
public class FacManualTarifarioFacade extends AbstractFacade<FacManualTarifario> {

    public FacManualTarifarioFacade() {
        super(FacManualTarifario.class);
    }

    public List<FacManualTarifario> buscarOrdenado() {
        try {
            String hql = "SELECT m FROM FacManualTarifario m ORDER By m.idManualTarifario";
            return getEntityManager().createQuery(hql).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<FacManualTarifario> buscarNoAsociados() {//buscar manuales tarifarios sin contrato asociado
        try {
            String sql = " select * from fac_manual_tarifario where id_manual_tarifario not in \n"
                    + " (select distinct(id_manual_tarifario) from fac_contrato);";
            return (List<FacManualTarifario>) getEntityManager().createNativeQuery(sql, FacManualTarifario.class).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

}
