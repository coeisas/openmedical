/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.fachadas;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import modelo.entidades.CitCitas;
import modelo.entidades.CitPropositoConsulta;

/**
 *
 * @author Mario
 */
@Stateless
public class CitPropositoConsultaFacade extends AbstractFacade<CitPropositoConsulta> {

    public CitPropositoConsultaFacade() {
        super(CitPropositoConsulta.class);
    }

    public List<CitPropositoConsulta> propositoConsultaByCita(CitCitas cita) {
        try {
            Query query = getEntityManager().createQuery("SELECT p FROM CitPropositoConsulta p WHERE p.citCitas = ?1");
            query.setParameter(1, cita);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

}
