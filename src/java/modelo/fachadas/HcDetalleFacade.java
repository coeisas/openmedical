/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.fachadas;

import javax.ejb.Stateless;
import javax.persistence.Query;
import modelo.entidades.HcDetalle;

/**
 *
 * @author santos
 */
@Stateless
public class HcDetalleFacade extends AbstractFacade<HcDetalle> {

    public HcDetalleFacade() {
        super(HcDetalle.class);
    }

    public String ultimoDxHC(int idPaciente) {
        try {
            Query query = getEntityManager().createNativeQuery(
                    "SELECT  substring(valor, 1, 4) FROM hc_detalle "
                    + "JOIN hc_registro ON hc_registro.id_registro = hc_detalle.id_registro JOIN cfg_pacientes ON cfg_pacientes.id_paciente = hc_registro.id_paciente "
                    + "WHERE id_campo IN (33,107,137) AND cfg_pacientes.id_paciente = " + idPaciente
                    + " ORDER BY hc_registro.fecha_reg DESC LIMIT 1");
            return query.getSingleResult().toString();
        } catch (Exception e) {
            return null;
        }
    }

}
