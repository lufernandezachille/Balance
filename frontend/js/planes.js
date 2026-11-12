const API_URL = "http://localhost:3000/api";

const btnGratis = document.getElementById("btn-seleccionar-gratis");
const btnPremium = document.getElementById("btn-seleccionar-premium");

async function cambiarPlan(nuevoPlan) {
    const token = localStorage.getItem("authToken");

    if (!token) {
        alert("Debes iniciar sesión para realizar esta acción.");
        window.location.href = "iniciarsesion.html";
        return;
    }

    try {
        const respuesta = await fetch(`${API_URL}/perfil/plan`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ plan: nuevoPlan })
        });

        if (!respuesta.ok) throw new Error("No se pudo actualizar el plan.");

        alert(`¡Felicidades! Tu cuenta ahora es ${nuevoPlan}.`);
        window.location.href = "configuracion.html";

    } catch (error) {
        alert(error.message);
    }
}

if (btnGratis) {
    btnGratis.addEventListener("click", () => cambiarPlan("Gratuito"));
}

if (btnPremium) {
    btnPremium.addEventListener("click", () => cambiarPlan("Premium"));
}