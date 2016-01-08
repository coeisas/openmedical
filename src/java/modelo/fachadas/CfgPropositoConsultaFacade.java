/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.fachadas;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import modelo.entidades.CfgPropositoConsulta;

/**
 *
 * @author Mario
 */
@Stateless
public class CfgPropositoConsultaFacade extends AbstractFacade<CfgPropositoConsulta> {

    public CfgPropositoConsultaFacade() {
        super(CfgPropositoConsulta.class);
    }
    
    public List<CfgPropositoConsulta> buscarPropositos(){
        try {
            Query query = getEntityManager().createQuery("SELECT p FROM CfgPropositoConsulta p ORDER BY p.idProposito");
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
}
