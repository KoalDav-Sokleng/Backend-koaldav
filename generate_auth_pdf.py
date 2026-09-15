import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY

def build_auth_pdf(filename="Authentication_and_OTP_Architecture.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        rightMargin=45,
        leftMargin=45,
        topMargin=45,
        bottomMargin=45
    )

    styles = getSampleStyleSheet()
    
    # Custom styles
    primary_color = colors.HexColor("#1E3A8A")   # Deep Indigo / Navy
    secondary_color = colors.HexColor("#0D9488") # Teal / Emerald
    dark_neutral = colors.HexColor("#1F2937")    # Slate 800
    light_bg = colors.HexColor("#F8FAFC")        # Slate 50
    accent_blue = colors.HexColor("#3B82F6")     # Bright Blue
    border_color = colors.HexColor("#E2E8F0")

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=primary_color,
        alignment=TA_CENTER
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=secondary_color,
        alignment=TA_CENTER
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        textColor=primary_color,
        spaceBefore=12,
        spaceAfter=6
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=secondary_color,
        spaceBefore=8,
        spaceAfter=4
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=dark_neutral,
        spaceAfter=5
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=11,
        textColor=colors.HexColor("#0F172A")
    )

    callout_style = ParagraphStyle(
        'Callout_Text',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor("#1E293B")
    )

    story = []

    # Title Banner
    story.append(Spacer(1, 10))
    story.append(Paragraph("Spring Boot Authentication & OTP Architecture", title_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph("Complete Technical Guide: Registration, OTP Email Verification, JWT Login & Password Reset", subtitle_style))
    story.append(Spacer(1, 10))
    story.append(HRFlowable(width="100%", thickness=1.5, color=primary_color, spaceBefore=4, spaceAfter=14))

    # Executive Overview
    story.append(Paragraph("1. Executive Overview", h1_style))
    overview_text = (
        "This service implements a production-grade, stateless security system using <b>Spring Security 6</b>, "
        "<b>JJWT (JSON Web Tokens)</b>, <b>PostgreSQL persistence</b>, and <b>JavaMailSender (Gmail SMTP)</b>. "
        "User accounts require two-factor verification via secure 6-digit one-time passwords (OTP) delivered to the "
        "user's registered email before access is granted."
    )
    story.append(Paragraph(overview_text, body_style))
    story.append(Spacer(1, 6))

    # Architecture Highlights Table
    arch_data = [
        [Paragraph("<b>Component</b>", body_style), Paragraph("<b>Implementation Detail</b>", body_style)],
        [Paragraph("Password Security", body_style), Paragraph("BCrypt strong hashing algorithm via BCryptPasswordEncoder", body_style)],
        [Paragraph("Session Management", body_style), Paragraph("Stateless JWT via JwtAuthFilter (Authorization: Bearer &lt;token&gt;)", body_style)],
        [Paragraph("OTP Generation", body_style), Paragraph("6-digit cryptographically secure number (SecureRandom), 5-minute expiry", body_style)],
        [Paragraph("Mail Transport", body_style), Paragraph("Gmail SMTP (smtp.gmail.com:587) with STARTTLS encryption", body_style)],
        [Paragraph("Database Engine", body_style), Paragraph("PostgreSQL dialect with JPA Hibernate auto-update DDL", body_style)]
    ]
    arch_table = Table(arch_data, colWidths=[140, 380])
    arch_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#E2E8F0")),
        ('GRID', (0, 0), (-1, -1), 0.5, border_color),
        ('PADDING', (0, 0), (-1, -1), 5),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(arch_table)
    story.append(Spacer(1, 12))

    # Section 2: Registration & Verification Flow
    story.append(Paragraph("2. User Registration & Email Verification Process", h1_style))
    story.append(Paragraph("Account onboarding enforces email validation before allowing any login activity:", body_style))
    
    steps_reg = [
        "<b>Step 1 (Submission):</b> The user submits <code>POST /api/auth/register</code> with <code>firstName</code>, <code>lastName</code>, <code>email</code>, and <code>password</code>.",
        "<b>Step 2 (Account Creation):</b> The system verifies the email is not already taken, hashes the password using <code>BCrypt</code>, and inserts the user record with <b><code>isVerified = false</code></b>.",
        "<b>Step 3 (OTP Dispatch):</b> A 6-digit random code is generated, stored in the <code>password_reset_otp</code> table with a 5-minute expiration timestamp, and emailed via Gmail SMTP.",
        "<b>Step 4 (OTP Verification):</b> The user submits <code>POST /api/auth/verify-registration-otp</code> with their email and 6-digit code.",
        "<b>Step 5 (Activation & Token):</b> The server verifies that the OTP is correct, unused, and unexpired. Upon validation, <code>isVerified</code> is updated to <code>true</code>, the OTP is marked used, and a signed <b>JWT AuthResponse</b> is returned."
    ]
    for s in steps_reg:
        story.append(Paragraph(f"• {s}", body_style))
    story.append(Spacer(1, 10))

    # Section 3: Login & JWT Authentication
    story.append(Paragraph("3. Login & JWT Session Management", h1_style))
    login_steps = [
        "<b>Step 1:</b> User submits <code>POST /api/auth/login</code> with email and password.",
        "<b>Step 2 (Verification Gate):</b> The service checks <code>user.isVerified()</code>. If <code>false</code>, it aborts immediately with <code>400 Bad Request: 'Please verify your email before logging in'</code>.",
        "<b>Step 3 (Credential Match):</b> <code>AuthenticationManager</code> verifies the raw password against the stored BCrypt hash.",
        "<b>Step 4 (Token Generation):</b> <code>JwtUtil</code> generates a compact, cryptographically signed JWT token embedding username and <code>USER</code> authorities.",
        "<b>Step 5 (Protected Requests):</b> The client includes <code>Authorization: Bearer &lt;token&gt;</code> in all protected requests. <code>JwtAuthFilter</code> validates the signature on each request."
    ]
    for s in login_steps:
        story.append(Paragraph(f"• {s}", body_style))
    story.append(Spacer(1, 10))

    # Section 4: Forgot Password Flow
    story.append(Paragraph("4. Forgot Password & Recovery Flow", h1_style))
    pwd_steps = [
        "<b>Step 1 (Request Code):</b> User sends <code>POST /api/auth/forgot-password</code> with their registered email.",
        "<b>Step 2 (OTP Issuance):</b> An unguessable 6-digit OTP is generated, persisted with a 5-minute expiry, and emailed to the user.",
        "<b>Step 3 (Reset Password):</b> User sends <code>POST /api/auth/reset-password</code> with <code>email</code>, <code>otpCode</code>, and <code>newPassword</code>.",
        "<b>Step 4 (Execution):</b> The OTP is verified and marked used; the user's password is encrypted and updated in PostgreSQL."
    ]
    for s in pwd_steps:
        story.append(Paragraph(f"• {s}", body_style))
    story.append(Spacer(1, 12))

    # Section 5: API Endpoints Summary Table
    story.append(Paragraph("5. API Endpoints Reference", h1_style))
    api_data = [
        [Paragraph("<b>Endpoint</b>", body_style), Paragraph("<b>Method</b>", body_style), Paragraph("<b>Request Payload</b>", body_style), Paragraph("<b>Success Response</b>", body_style)],
        [
            Paragraph("<code>/api/auth/register</code>", code_style),
            Paragraph("POST", body_style),
            Paragraph("{firstName, lastName, email, password}", code_style),
            Paragraph("200 OK ('Registered. OTP sent...')", body_style)
        ],
        [
            Paragraph("<code>/api/auth/verify-registration-otp</code>", code_style),
            Paragraph("POST", body_style),
            Paragraph("{email, otpCode}", code_style),
            Paragraph("200 OK (JWT AuthResponse)", body_style)
        ],
        [
            Paragraph("<code>/api/auth/login</code>", code_style),
            Paragraph("POST", body_style),
            Paragraph("{email, password}", code_style),
            Paragraph("200 OK (JWT AuthResponse)", body_style)
        ],
        [
            Paragraph("<code>/api/auth/forgot-password</code>", code_style),
            Paragraph("POST", body_style),
            Paragraph("{email}", code_style),
            Paragraph("200 OK ('OTP sent to email')", body_style)
        ],
        [
            Paragraph("<code>/api/auth/reset-password</code>", code_style),
            Paragraph("POST", body_style),
            Paragraph("{email, otpCode, newPassword}", code_style),
            Paragraph("200 OK ('Password reset successfully')", body_style)
        ]
    ]
    api_table = Table(api_data, colWidths=[150, 45, 175, 150])
    api_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('GRID', (0, 0), (-1, -1), 0.5, border_color),
        ('PADDING', (0, 0), (-1, -1), 5),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, light_bg])
    ]))
    story.append(api_table)
    story.append(Spacer(1, 14))

    # Security Best Practices Callout Box
    callout_data = [[
        Paragraph(
            "<b>🛡️ Security Safeguards:</b><br/>"
            "• <b>Single-Use OTPs:</b> Once validated, OTPs are immediately flagged as <code>used = true</code> in the DB to prevent replay attacks.<br/>"
            "• <b>Time-Bound Expiration:</b> Codes expire after exactly 5 minutes (<code>LocalDateTime.now().plusMinutes(5)</code>).<br/>"
            "• <b>Salted BCrypt:</b> Passwords are never stored in plaintext and cannot be reverse-engineered.<br/>"
            "• <b>Stateless Sessions:</b> No session memory is retained on the server, ensuring infinite horizontal scalability.",
            callout_style
        )
    ]]
    callout_table = Table(callout_data, colWidths=[520])
    callout_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
        ('BOX', (0, 0), (-1, -1), 1, accent_blue),
        ('PADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(callout_table)

    doc.build(story)
    print("PDF generated successfully:", filename)

if __name__ == "__main__":
    build_auth_pdf()

