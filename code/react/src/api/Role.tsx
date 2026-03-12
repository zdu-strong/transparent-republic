import { SystemRoleModel } from "@/model/SystemRoleModel";
import axios from "axios";
import { TypedJSON } from "typedjson";

export async function getRoleById(roleId: string) {
    const { data } = await axios.get("/role", { params: { id: roleId } });
    return new TypedJSON(SystemRoleModel).parse(data)!;
}
