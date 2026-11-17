const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("authToken");

const btnGratis = document.getElementById("btn-seleccionar-gratis");
const btnPremium = document.getElementById("btn-seleccionar-premium");

async function cargarEstadoPlanes() {
    if (!token) {
        alert("Debes iniciar sesión para ver los planes.");
        window.location.href = "iniciarsesion.html";
        return;
    }

    try {
        const respuesta = await fetch(`${API_URL}/usuarios/suscripcion`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!respuesta.ok) return;

        const suscripcion = await respuesta.json();
        const esPremiumActivo =
            suscripcion.nombrePlan === "PREMIUM" && suscripcion.estado === "ACTIVA";

        if (esPremiumActivo) {
            if (btnPremium) {
                btnPremium.textContent = "Plan actual";
                btnPremium.disabled = true;
            }
            if (btnGratis) {
                btnGratis.textContent = "Volver a Gratuito";
            }
        } else {
            if (btnGratis) {
                btnGratis.textContent = "Plan actual";
                btnGratis.disabled = true;
            }
            if (btnPremium) {
                btnPremium.textContent = "Obtener Premium";
            }
        }
    } catch (error) {
        console.error("Error al consultar la suscripción:", error);
    }
}

if (btnGratis) {
    btnGratis.addEventListener("click", async () => {
        if (!confirm("¿Querés cancelar tu suscripción Premium y volver al plan Gratuito?")) {
            return;
        }

        try {
            const respuesta = await fetch(`${API_URL}/usuarios/suscripcion/cancelar`, {
                method: "POST",
                headers: { "Authorization": `Bearer ${token}` }
            });

            if (!respuesta.ok) throw new Error("No se pudo cambiar al plan Gratuito.");

            alert("Tu cuenta volvió al plan Gratuito.");
            window.location.href = "configuracion.html";
        } catch (error) {
            alert(error.message);
        }
    });
}

if (btnPremium) {
    btnPremium.addEventListener("click", () => {
        window.location.href = "planpremium.html";
    });
}

document.addEventListener("DOMContentLoaded", cargarEstadoPlanes);