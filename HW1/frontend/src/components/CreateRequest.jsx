import React, { useState, useEffect } from 'react';
import { getMunicipalities, createServiceRequest } from '../services/api';
import './CreateRequest.css';

const CreateRequest = () => {
  const [municipalities, setMunicipalities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [success, setSuccess] = useState(null);
  const [token, setToken] = useState(null);

  const [formData, setFormData] = useState({
    municipalityCode: '',
    municipalityName: '',
    citizenName: '',
    citizenEmail: '',
    citizenPhone: '',
    pickupAddress: '',
    itemDescription: '',
    preferredDate: '',
    preferredTimeSlot: 'MORNING',
  });

  useEffect(() => {
    loadMunicipalities();
  }, []);

  const loadMunicipalities = async () => {
    try {
      const data = await getMunicipalities();
      setMunicipalities(data);
    } catch (err) {
      console.error('Error loading municipalities:', err);
      const errorMessage = err.apiError?.message || 'Failed to load municipalities. Please try again.';
      setError(errorMessage);
    }
  };

  const handleMunicipalityChange = (e) => {
    const selectedMunicipality = municipalities.find(
      (m) => m.code === e.target.value
    );
    if (selectedMunicipality) {
      setFormData({
        ...formData,
        municipalityCode: selectedMunicipality.code,
        municipalityName: selectedMunicipality.name,
      });
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    // Clear field-specific error when user starts typing
    if (fieldErrors[name]) {
      setFieldErrors({ ...fieldErrors, [name]: null });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setFieldErrors({});
    setSuccess(null);

    try {
      const response = await createServiceRequest(formData);
      setSuccess('Request created successfully!');
      setToken(response.token);
      
      // Reset form
      setFormData({
        municipalityCode: '',
        municipalityName: '',
        citizenName: '',
        citizenEmail: '',
        citizenPhone: '',
        pickupAddress: '',
        itemDescription: '',
        preferredDate: '',
        preferredTimeSlot: 'MORNING',
      });
    } catch (err) {
      console.error('Error creating request:', err);
      
      // Use structured error from interceptor
      if (err.apiError) {
        if (err.apiError.errors) {
          // Validation errors - set per field
          setFieldErrors(err.apiError.errors);
        } else {
          setError(err.apiError.message);
        }
      } else {
        setError('Failed to create request. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="create-request-container">
      <h1>Request Waste Collection</h1>
      <p className="subtitle">Schedule a pickup for your large items</p>

      {error && <div className="alert alert-error">{error}</div>}
      {success && (
        <div className="alert alert-success">
          <p>{success}</p>
          <p className="token-display">
            <strong>Your Access Token:</strong>
            <br />
            <code>{token}</code>
          </p>
          <p className="token-note">
            ⚠️ Save this token! You'll need it to check or cancel your request.
          </p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="request-form">
        <div className="form-section">
          <h2>Location Information</h2>

          <div className="form-group">
            <label htmlFor="municipality">
              Municipality <span className="required">*</span>
            </label>
            <select
              id="municipality"
              name="municipalityCode"
              value={formData.municipalityCode}
              onChange={handleMunicipalityChange}
            >
              <option value="">Select a municipality</option>
              {municipalities.map((municipality) => (
                <option key={municipality.code} value={municipality.code}>
                  {municipality.name}
                </option>
              ))}
            </select>
            {fieldErrors.municipalityCode && (
              <div className="field-error">{fieldErrors.municipalityCode}</div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="pickupAddress">
              Pickup Address <span className="required">*</span>
            </label>
            <input
              type="text"
              id="pickupAddress"
              name="pickupAddress"
              value={formData.pickupAddress}
              onChange={handleChange}
              placeholder="Street, number, postal code"
              maxLength={200}
            />
            {fieldErrors.pickupAddress && (
              <div className="field-error">{fieldErrors.pickupAddress}</div>
            )}
          </div>
        </div>

        <div className="form-section">
          <h2>Contact Information</h2>

          <div className="form-group">
            <label htmlFor="citizenName">
              Full Name <span className="required">*</span>
            </label>
            <input
              type="text"
              id="citizenName"
              name="citizenName"
              value={formData.citizenName}
              onChange={handleChange}
              placeholder="Your full name"
              maxLength={100}
            />
            {fieldErrors.citizenName && (
              <div className="field-error">{fieldErrors.citizenName}</div>
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="citizenEmail">Email</label>
              <input
                type="email"
                id="citizenEmail"
                name="citizenEmail"
                value={formData.citizenEmail}
                onChange={handleChange}
                placeholder="your@email.com"
                maxLength={100}
              />
              {fieldErrors.citizenEmail && (
                <div className="field-error">{fieldErrors.citizenEmail}</div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="citizenPhone">
                Phone <span className="required">*</span>
              </label>
              <input
                type="tel"
                id="citizenPhone"
                name="citizenPhone"
                value={formData.citizenPhone}
                onChange={handleChange}
                placeholder="912345678"
                pattern="[0-9]{9}"
                title="Phone must be 9 digits"
              />
              {fieldErrors.citizenPhone && (
                <div className="field-error">{fieldErrors.citizenPhone}</div>
              )}
            </div>
          </div>
        </div>

        <div className="form-section">
          <h2>Collection Details</h2>

          <div className="form-group">
            <label htmlFor="itemDescription">
              Item Description <span className="required">*</span>
            </label>
            <textarea
              id="itemDescription"
              name="itemDescription"
              value={formData.itemDescription}
              onChange={handleChange}
              placeholder="Describe the items you want to dispose (min 10 characters)"
              rows={4}
              minLength={10}
              maxLength={500}
            />
            <small className="char-counter">
              {formData.itemDescription.length}/500 characters
            </small>
            {fieldErrors.itemDescription && (
              <div className="field-error">{fieldErrors.itemDescription}</div>
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="preferredDate">
                Preferred Date <span className="required">*</span>
              </label>
              <input
                type="date"
                id="preferredDate"
                name="preferredDate"
                value={formData.preferredDate}
                onChange={handleChange}
              />
              {fieldErrors.preferredDate && (
                <div className="field-error">{fieldErrors.preferredDate}</div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="preferredTimeSlot">
                Time Slot <span className="required">*</span>
              </label>
              <select
                id="preferredTimeSlot"
                name="preferredTimeSlot"
                value={formData.preferredTimeSlot}
                onChange={handleChange}
              >
                <option value="MORNING">Morning (08:00 - 12:00)</option>
                <option value="AFTERNOON">Afternoon (12:00 - 18:00)</option>
                <option value="EVENING">Evening (18:00 - 21:00)</option>
              </select>
              {fieldErrors.preferredTimeSlot && (
                <div className="field-error">{fieldErrors.preferredTimeSlot}</div>
              )}
            </div>
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Request'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateRequest;

